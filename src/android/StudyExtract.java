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

public class StudyExtract extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("extractFromFile")) {
            JsonReader reader;

            String progress = "nothing";
            Gson gson = new Gson();
            ObservationPlot obv;
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

                callbackContext.success(gson.toJson(obv));
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error("error:");

            }

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
            return "ObservationPlot [plotNumber=" + plotNumber + ", pedigree=" + pedigree + ", germplasmDbId="
                    + germplasmDbId + ", observationUnitName=" + observationUnitName + ", plantNumber=" + plantNumber
                    + ", entryNumber=" + entryNumber + ", replication=" + replication + ", entryType=" + entryType
                    + ", observationUnitDbId=" + observationUnitDbId + ", Y=" + Y + ", X=" + X + ", blockNumber="
                    + blockNumber + ", additionalInfo=" + additionalInfo + ", observations=" + observations + "]";
        }

    }

    public class PlotObservationAdditionalInfo {
        private String plotKey;

        private String plotCode;

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
            return "ClassPojo [collector = " + collector + ", observationVariableId = " + observationVariableId
                    + ", value = " + value + ", observationTimeStamp = " + observationTimeStamp + ", observationDbId = "
                    + observationDbId + ", observationVariableName = " + observationVariableName + "]";
        }
    }

}
