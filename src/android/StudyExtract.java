package org.irri.breeding4rice.cordova;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.Permission;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.content.ContentValues;

public class StudyExtract extends CordovaPlugin {

  public SQLiteDatabase database;
  String OBSERVATION_PLOT_TABLE = "ObservationPlot";
  String CREATE_OBSERVATION_PLOT_TABLE = "CREATE TABLE `" + OBSERVATION_PLOT_TABLE + "` ("
      + "	`id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,`seq_x` INTEGER NOT NULL,`seq_y` INTEGER NOT NULL,"
      + "	`study`	TEXT," + "	`observationUnitDbId`	TEXT," + "	`observationUnitName`	TEXT,"
      + "	`germplasmDbId`	TEXT," + "	`pedigree`	TEXT," + "	`entryNumber`	TEXT," + "	`plotNumber`	INTEGER,"
      + "	`plantNumber`	TEXT," + "	`blockNumber`	TEXT," + "	`designation`	TEXT," + "	`generation`	TEXT,"
      + "	`plotCode`	TEXT," + "	`plotKey`	TEXT," + "	`X`	INTEGER," + "	`Y`	INTEGER,"
      + "	`replication`	TEXT," + "	`isModified`	BOOLEAN, `lastModified`   DATETIME" + ");";

  String OBSERVATION_DATA_TABLE = "ObservationData";
  String CREATE_OBSERVATION_DATA = "CREATE TABLE `" + OBSERVATION_DATA_TABLE + "` ("
      + "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + "`observationUnitDbId` TEXT," + "`observationDbId` TEXT,"
      + "`observationVariableName` TEXT," + "`observationVariableId` TEXT," + "`collector` TEXT,"
      + "`observationTimeStamp` TEXT," + "`value` TEXT," + "`synced` BOOLEAN," + "`status` TEXT" + ");";
  String CREATE_OBSERVATION_AUDITLOGS = "CREATE TABLE `ObservationAuditLogs` ("
      + "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + "`observationDbId` TEXT," + "`modifiedValues` TEXT,"
      + "`collector` TEXT," + "`observationTimeStamp` TEXT" + ");";

  @Override
  public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext)
      throws JSONException {

    if (action.equals("extractFromFile")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          JsonReader reader;

          String progress = "nothing";
          Gson gson = new Gson();
          ObservationPlot obv = new ObservationPlot();
          try {
            reader = new JsonReader(new FileReader(data.getString(0)));
            reader.beginObject();
            String endPlot = "";
            while (reader.hasNext()) {
              String name = reader.nextName();

              if (name.equals("result")) {
                progress = "result in";
                reader.beginObject();

                while (reader.hasNext()) {
                  String result_name = reader.nextName();
                  if (result_name.equals("data")) {
                    progress = "data in";
                    reader.beginArray();
                    Integer loaded = 0;
                    while (reader.hasNext()) {
                      System.out.println("reader " + loaded++);
                      obv = gson.fromJson(reader, ObservationPlot.class);
                      //                        		reader.skipValue();
                      // System.out.println(gson.toJson(obv));
                      PluginResult result = new PluginResult(PluginResult.Status.OK, gson.toJson(obv));
                      result.setKeepCallback(true);
                      callbackContext.sendPluginResult(result);

                    }
                    reader.endArray();
                  } else {
                    reader.skipValue();
                  }

                }

                reader.endObject();
              } else {// unexpected value, skip it or generate error
                reader.skipValue();
              }
            }

            reader.endObject();
            reader.close();

            callbackContext.success("{\"status\":\"done\"}");
          } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("error:");

          }
        }
      });

      return true;

    } else if (action.equals("extractToSql")) {

      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          JsonReader reader;

          String progress = "nothing";

          Gson gson = new Gson();
          ObservationPlot obv = new ObservationPlot();
          try {

            String studyName = data.getString(0);
            String mainFolderPath = data.getString(1);
            String extractedJsonPath = data.getString(2);
            File file = new File(mainFolderPath, studyName + ".db");
            database = SQLiteDatabase.openOrCreateDatabase(file, null);
            database.execSQL(CREATE_OBSERVATION_PLOT_TABLE);
            database.execSQL(CREATE_OBSERVATION_DATA);
            database.execSQL(CREATE_OBSERVATION_AUDITLOGS);
            database.beginTransaction();

            reader = new JsonReader(new FileReader(extractedJsonPath));
            reader.beginObject();
            String endPlot = "";
            while (reader.hasNext()) {
              String name = reader.nextName();

              if (name.equals("result")) {
                progress = "result in";
                reader.beginObject();

                while (reader.hasNext()) {
                  String result_name = reader.nextName();
                  if (result_name.equals("data")) {
                    progress = "data in";
                    reader.beginArray();
                    Integer loaded = 0;
                    Integer seq_id = 1;
                    while (reader.hasNext()) {
                      System.out.println("reader " + loaded++);
                      obv = gson.fromJson(reader, ObservationPlot.class);
                      //                        		reader.skipValue();
                      // System.out.println(gson.toJson(obv));
                      ContentValues plotValues = new ContentValues();
                      plotValues.put("seq_x", obv.getAdditionalInfo().getSeqHorizontal());
                      plotValues.put("seq_y", obv.getAdditionalInfo().getSeqVertical());

                      plotValues.put("observationUnitDbId", obv.getObservationUnitDbId());
                      plotValues.put("designation", obv.getAdditionalInfo().getDesignation());
                      plotValues.put("generation", obv.getAdditionalInfo().getGeneration());
                      plotValues.put("plotCode", obv.getAdditionalInfo().getPlotCode());
                      plotValues.put("plotKey", obv.getAdditionalInfo().getPlotKey());
                      plotValues.put("blockNumber", obv.getBlockNumber());
                      plotValues.put("entryNumber", obv.getEntryNumber());
                      plotValues.put("germplasmDbId", obv.getGermplasmDbId());
                      plotValues.put("observationUnitName", obv.getObservationUnitName());
                      plotValues.put("pedigree", obv.getPedigree());
                      plotValues.put("plantNumber", obv.getPlantNumber());
                      plotValues.put("plotNumber", obv.getPlotNumber());
                      plotValues.put("replication", obv.getReplication());
                      plotValues.put("X", Integer.parseInt(obv.getX()));
                      plotValues.put("Y", Integer.parseInt(obv.getY()));
                      plotValues.put("isModified", false);
                      plotValues.put("study", studyName);
                      database.insert(OBSERVATION_PLOT_TABLE, null, plotValues);
                      for (PlotObservationData obvData : obv.getObservations()) {
                        ContentValues observationValues = new ContentValues();
                        observationValues.put("collector", obvData.getCollector());
                        observationValues.put("observationUnitDbId", obv.getObservationUnitDbId());

                        observationValues.put("observationDbId", obvData.getObservationDbId());
                        observationValues.put("observationTimeStamp", obvData.getObservationTimeStamp());
                        observationValues.put("observationVariableId", obvData.getObservationVariableId());
                        observationValues.put("observationVariableName", obvData.getObservationVariableName());

                        observationValues.put("value", obvData.getValue());

                        observationValues.put("synced", true);
                        observationValues.put("status", "synced");
                        database.insert(OBSERVATION_DATA_TABLE, null, observationValues);

                      }

                    }
                    reader.endArray();
                  } else {
                    reader.skipValue();
                  }

                }

                reader.endObject();
              } else {// unexpected value, skip it or generate error
                reader.skipValue();
              }
            }

            reader.endObject();
            reader.close();
            database.setTransactionSuccessful();
            database.endTransaction();

            callbackContext.success("{\"status\":\"done\",\"error\":\"false\"}");
          } catch (Exception e) {
            e.printStackTrace();
            callbackContext.error("{\"status\":\"error:\" + " + e.toString() + ",\"error\":\"true\"}");

          }
        }
      });

      return true;
    } else {

      return false;

    }
  }

  public class ObservationPlot {
    private String plotNumber;

    private String pedigree;

    private String germplasmDbId;

    private String observationUnitName;

    private String plantNumber;

    private String entryNumber;

    private String replication;

    private String entryType;

    private String observationUnitDbId;

    private String Y;

    private String X;

    private String blockNumber;

    private PlotObservationAdditionalInfo additionalInfo;

    public List<PlotObservationData> getObservations() {
      return observations;
    }

    public void setObservations(List<PlotObservationData> observations) {
      this.observations = observations;
    }

    private List<PlotObservationData> observations;

    public PlotObservationAdditionalInfo getAdditionalInfo() {
      return additionalInfo;
    }

    public void setAdditionalInfo(PlotObservationAdditionalInfo additionalInfo) {
      this.additionalInfo = additionalInfo;
    }

    public String getPlotNumber() {
      return plotNumber;
    }

    public void setPlotNumber(String plotNumber) {
      this.plotNumber = plotNumber;
    }

    public String getPedigree() {
      return pedigree;
    }

    public void setPedigree(String pedigree) {
      this.pedigree = pedigree;
    }

    public String getGermplasmDbId() {
      return germplasmDbId;
    }

    public void setGermplasmDbId(String germplasmDbId) {
      this.germplasmDbId = germplasmDbId;
    }

    public String getObservationUnitName() {
      return observationUnitName;
    }

    public void setObservationUnitName(String observationUnitName) {
      this.observationUnitName = observationUnitName;
    }

    public String getPlantNumber() {
      return plantNumber;
    }

    public void setPlantNumber(String plantNumber) {
      this.plantNumber = plantNumber;
    }

    public String getEntryNumber() {
      return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
      this.entryNumber = entryNumber;
    }

    public String getReplication() {
      return replication;
    }

    public void setReplication(String replication) {
      this.replication = replication;
    }

    public String getEntryType() {
      return entryType;
    }

    public void setEntryType(String entryType) {
      this.entryType = entryType;
    }

    public String getObservationUnitDbId() {
      return observationUnitDbId;
    }

    public void setObservationUnitDbId(String observationUnitDbId) {
      this.observationUnitDbId = observationUnitDbId;
    }

    public String getY() {
      return Y;
    }

    public void setY(String Y) {
      this.Y = Y;
    }

    public String getX() {
      return X;
    }

    public void setX(String X) {
      this.X = X;
    }

    public String getBlockNumber() {
      return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
      this.blockNumber = blockNumber;
    }

    @Override
    public String toString() {
      return "ObservationPlot [plotNumber=" + plotNumber + ", pedigree=" + pedigree + ", germplasmDbId=" + germplasmDbId
          + ", observationUnitName=" + observationUnitName + ", plantNumber=" + plantNumber + ", entryNumber="
          + entryNumber + ", replication=" + replication + ", entryType=" + entryType + ", observationUnitDbId="
          + observationUnitDbId + ", Y=" + Y + ", X=" + X + ", blockNumber=" + blockNumber + ", additionalInfo="
          + additionalInfo + ", observations=" + observations + "]";
    }

  }

  public class PlotObservationAdditionalInfo {
    private String plotKey;

    private String plotCode;
    private String designation;
    private String generation;

    private Integer seqHorizontal;
    private Integer seqVertical;

    public Integer getSeqHorizontal() {
      return seqHorizontal;
    }

    public void setSeqHorizontal(Integer seqHorizontal) {
      this.seqHorizontal = seqHorizontal;
    }

    public Integer getSeqVertical() {
      return seqVertical;
    }

    public void setSeqVertical(Integer seqVertical) {
      this.seqVertical = seqVertical;
    }
    public String getDesignation() {
      return designation;
    }

    public void setDesignation(String designation) {
      this.designation = designation;
    }

    public String getGeneration() {
      return generation;
    }

    public void setGeneration(String generation) {
      this.generation = generation;
    }

    public String getPlotKey() {
      return plotKey;
    }

    public void setPlotKey(String plotKey) {
      this.plotKey = plotKey;
    }

    public String getPlotCode() {
      return plotCode;
    }

    public void setPlotCode(String plotCode) {
      this.plotCode = plotCode;
    }

    @Override
    public String toString() {
      return "ClassPojo [plotKey = " + plotKey + ", plotCode = " + plotCode + "]";
    }
  }

  public class PlotObservationData {
    private String collector;

    private String observationVariableId;

    private String value;

    private String observationTimeStamp;

    private String observationDbId;

    private String observationVariableName;

    public String getCollector() {
      return collector;
    }

    public void setCollector(String collector) {
      this.collector = collector;
    }

    public String getObservationVariableId() {
      return observationVariableId;
    }

    public void setObservationVariableId(String observationVariableId) {
      this.observationVariableId = observationVariableId;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getObservationTimeStamp() {
      return observationTimeStamp;
    }

    public void setObservationTimeStamp(String observationTimeStamp) {
      this.observationTimeStamp = observationTimeStamp;
    }

    public String getObservationDbId() {
      return observationDbId;
    }

    public void setObservationDbId(String observationDbId) {
      this.observationDbId = observationDbId;
    }

    public String getObservationVariableName() {
      return observationVariableName;
    }

    public void setObservationVariableName(String observationVariableName) {
      this.observationVariableName = observationVariableName;
    }

    @Override
    public String toString() {
      return "ClassPojo [collector = " + collector + ", observationVariableId = " + observationVariableId + ", value = "
          + value + ", observationTimeStamp = " + observationTimeStamp + ", observationDbId = " + observationDbId
          + ", observationVariableName = " + observationVariableName + "]";
    }
  }

}
