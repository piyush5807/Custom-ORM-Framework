package com.hibernate.reflections.entities;

import com.hibernate.reflections.annotations.*;

public class User {

    @Id
    private int id;

    @Column(value = "user_name")
    private String name;

    @Column
    private int age;

    @Column
    private Float balance;

    public User(int id, String name, int age, Float balance) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.balance = balance;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", balance=" + balance +
                '}';
    }
}
