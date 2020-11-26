package com.atguigu.gmall.list.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Document(indexName = "user",type = "info",shards = 3,replicas = 1)
public class User {

    @Autowired
    static ElasticsearchRestTemplate elasticsearchRestTemplate;

    public static void main(String[] args) {
        elasticsearchRestTemplate.createIndex(User.class);
        elasticsearchRestTemplate.putMapping(User.class);
    }

    @Field(type = FieldType.Text,analyzer = "ik_max_word",index = true)
    private String name;

    @Field(type = FieldType.Long,index = true)
    private Long age;

    @Field(type = FieldType.Nested,index = true)
    private String[] users;

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }
}
