package com.example.android_iotproject;


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "BUSdhtTest")

public class DHTtable {
    private int _idx;
    private int _humid;
    private float _temp;
    private String _qr;

    @DynamoDBRangeKey(attributeName = "idx")
    @DynamoDBAttribute(attributeName = "idx")
    public int getId() {
        return _idx;
    }
    public void setId(int _idx) {
        this._idx = _idx;
    }

    @DynamoDBAttribute(attributeName = "humid")
    public int getHumid(){
        return _humid;
    }
    public void setHumid(int humid) {
        this._humid = humid;
    }

    @DynamoDBAttribute(attributeName = "temp")
    public float getTemp() {
        return _temp;
    }
    public void setTemp(float temp) {
        this._temp = temp;
    }

    //@DynamoDBRangeKey(attributeName = "qr")
    @DynamoDBHashKey(attributeName = "qr")
    @DynamoDBAttribute(attributeName = "qr")
    public String getqr() {
        return _qr;
    }
    public void setqr(String qr) {
        this._qr = qr;
    }
}
