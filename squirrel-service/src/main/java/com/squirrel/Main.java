package com.squirrel;

import com.google.gson.Gson;
import com.squirrel.model.ResponseResult;

public class Main {

    public static void main(String[] args) {
        Gson gson = new Gson();
        ResponseResult hello = ResponseResult.successResult("hello");
        ResponseResult damn = ResponseResult.errorResult(22, "damn!");
        System.out.println(gson.toJson(hello));
        System.out.println(gson.toJson(damn));
    }

}
