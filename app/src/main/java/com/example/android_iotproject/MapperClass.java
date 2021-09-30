package com.example.android_iotproject;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.Vector;

@DynamoDBTable(tableName="busStateDB")
public class MapperClass {
    String busNum;
    String bell;

    @DynamoDBHashKey(attributeName = "busNum")
    public String getBusNum() {
        return busNum;
    }

    public void setBusNum(String busNum) {
        this.busNum = busNum;
    }

    @DynamoDBAttribute(attributeName="bell")
    public String getBell() {
        return bell;
    }

    public void setBell(String bell) {
        this.bell = bell;
    }


}
