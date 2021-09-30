package com.example.android_iotproject;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import static com.estimote.sdk.EstimoteSDK.getApplicationContext;

public class ManagerClass {

    CognitoCachingCredentialsProvider credentialsProvider = null;
    CognitoSyncManager syncManager = null;
    public static AmazonDynamoDBClient dynamoDBClient = null;
    public static DynamoDBMapper dynamoDBMapper = null;

    public CognitoCachingCredentialsProvider getcredentials(Context context) {
        // Amazon Cognito 인증 공급자를 초기화합니다
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-northeast-2:f1e3aa02-a85b-4426-ba40-f8aaf69c363a", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );

        syncManager = new CognitoSyncManager(context, Regions.AP_NORTHEAST_2, credentialsProvider);
        Dataset dataset = syncManager.openOrCreateDataset("Mydataset");
        dataset.put("mykey", "myvalue");

        dataset.synchronize(new DefaultSyncCallback());
        return credentialsProvider;
    }

    public DynamoDBMapper initDynamoClient(CognitoCachingCredentialsProvider credentialsProvider){
        if(dynamoDBClient == null){
            dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
            dynamoDBClient.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
            dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);

        }
        return dynamoDBMapper;
    }
}
