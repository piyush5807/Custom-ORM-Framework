package com.hibernate.reflections;

import com.hibernate.reflections.annotations.Column;
import com.hibernate.reflections.annotations.Id;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class RepositoryImpl<T, ID> implements Repository<T, ID>{

    private Connection connection;
    private Class<T> typeParameterClass;
    private Class<ID> idParameterClass;

    public RepositoryImpl(Class<T> typeParameterClass, Class<ID> idParameterClass) throws Exception {
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/reflections_demo", "root", "");
        this.typeParameterClass = typeParameterClass;
        this.idParameterClass = idParameterClass;
        createTable();
    }

    private void createTable() throws Exception{
        Field[] fields = typeParameterClass.getDeclaredFields();

        StringJoiner columnsString = new StringJoiner(",");

        for(Field field : fields){
            field.setAccessible(true);
            if(field.isAnnotationPresent(Id.class)){
                Column columnAnnotation = field.getAnnotation(Column.class);
                String primaryKeyColumn = columnAnnotation == null ||
                        columnAnnotation.value() == null ||
                        columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();
                String sqlDataType = getSqlDataType(field.getType());
                columnsString.add(primaryKeyColumn + sqlDataType + " primary key");
            } else if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.value() == null
                        || columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();
                String sqlDataType = getSqlDataType(field.getType());
                columnsString.add(columnName + sqlDataType);
            }
        }

        String sqlQuery = "create table if not exists " + typeParameterClass.getSimpleName() + " ( " + columnsString + " ) ";

        Statement statement = this.connection.createStatement();
        statement.execute(sqlQuery);
    }

    private String getSqlDataType(Class<?> clss){
        if (int.class.equals(clss) || Integer.class.equals(clss)) {
            return " int ";
        } else if (long.class.equals(clss) || Long.class.equals(clss)) {
           return " bigint ";
        } else if (String.class.equals(clss)) {
            return " varchar(255) ";
        } else if (boolean.class.equals(clss) || Boolean.class.equals(clss)) {
            return " bool ";
        } else if (double.class.equals(clss) || Double.class.equals(clss)) {
            return " decimal(10, 2) ";
        } else if (float.class.equals(clss) || Float.class.equals(clss)) {
            return " float (10, 2) ";
        } else {
            throw new IllegalStateException("Unexpected value: " + idParameterClass);
        }
    }

    @Override
    public Collection<T> findAll() throws Exception {
        Field[] fields = typeParameterClass.getDeclaredFields();

        String query = "select * from " + typeParameterClass.getSimpleName();
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        List<T> result = new ArrayList<>();

        while(resultSet.next()){
            T t = (T) typeParameterClass.getConstructor().newInstance();
            for (Field field : fields) {
                field.setAccessible(true);

                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.value() == null ||
                        columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();

                setFields(field, resultSet, t, columnName);
            }
            result.add(t);
        }
        return result;
    }

    @Override
    public Optional<T> findById(ID id) throws Exception {
        Field[] fields = typeParameterClass.getDeclaredFields();
        List<String> columnNames = new ArrayList<>();

        String primaryKeyColumn = null;
        for(Field field : fields){
            field.setAccessible(true);
            if(field.isAnnotationPresent(Id.class)){
                Column columnAnnotation = field.getAnnotation(Column.class);
                primaryKeyColumn = columnAnnotation == null ||
                        columnAnnotation.value() == null ||
                        columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();
                columnNames.add(primaryKeyColumn);
            }else if(field.isAnnotationPresent(Column.class)){
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.value() == null
                        || columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();
                columnNames.add(columnName);
            }
        }

        if(primaryKeyColumn == null || primaryKeyColumn.length() == 0){
            throw new IllegalStateException("Primary key in java class is not defined");
        }

        String query = "select * from " + typeParameterClass.getSimpleName() + " where " + primaryKeyColumn + " = ?";
        PreparedStatement preparedStatement = this.connection.prepareStatement(query);

        setPreparedStatement(idParameterClass, preparedStatement, 1, id);
        ResultSet resultSet = preparedStatement.executeQuery();

        Optional<T> result = Optional.empty();

        while(resultSet.next()){
            T t = (T) typeParameterClass.getConstructor().newInstance();

            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                setFields(fields[i], resultSet, t, columnNames.get(i));
            }
            result = Optional.ofNullable(t);
        }

        return result;
    }

    @Override
    public void deleteById(ID id) throws Exception{
        Field[] fields = typeParameterClass.getDeclaredFields();

        String primaryKeyColumn = null;
        for(Field field : fields){
            field.setAccessible(true);
            if(field.isAnnotationPresent(Id.class)){
                Column columnAnnotation = field.getAnnotation(Column.class);
                primaryKeyColumn = columnAnnotation == null ||
                        columnAnnotation.value() == null ||
                        columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();
                break;
            }
        }

        if(primaryKeyColumn == null || primaryKeyColumn.length() == 0){
            throw new IllegalStateException("Primary key in java class is not defined");
        }

        String query = "delete from " + typeParameterClass.getSimpleName() + " where " + primaryKeyColumn + " = ?";
        PreparedStatement preparedStatement = this.connection.prepareStatement(query);


        setPreparedStatement(idParameterClass, preparedStatement, 1, id);

        int rowsDeleted = preparedStatement.executeUpdate();
        System.out.println("Number of rows deleted are " + rowsDeleted);

    }

    @Override
    public T save(T t) throws Exception{

        Field[] fields = typeParameterClass.getDeclaredFields();

        List<Field> columns = new ArrayList<>();
        StringJoiner columnsString = new StringJoiner(",");

        for(Field field : fields){
            field.setAccessible(true);
            if(field.isAnnotationPresent(Id.class)){
                Column columnAnnotation = field.getAnnotation(Column.class);
                String primaryKeyColumn = columnAnnotation == null ||
                        columnAnnotation.value() == null ||
                        columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();
                columnsString.add(primaryKeyColumn);
                columns.add(field);
            } else if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.value() == null
                        || columnAnnotation.value().length() == 0 ?
                        field.getName() : columnAnnotation.value();
                columnsString.add(columnName);
                columns.add(field);
            }
        }

        StringJoiner preparedStatementValueJoiner = new StringJoiner(",");
        for(int i = 0; i < columns.size(); i++){
            preparedStatementValueJoiner.add("? ");
        }

        int preparedStatementIndex = 1;


        String query = "insert into " + typeParameterClass.getSimpleName() + "(" + columnsString + " ) VALUES (" + preparedStatementValueJoiner + ")";
        PreparedStatement preparedStatement = this.connection.prepareStatement(query);

        for (Field field : columns) {
          if (int.class.equals(field.getType()) || Integer.class.equals(field.getType())) {
            preparedStatement.setInt(preparedStatementIndex++, (Integer) field.get(t));
          } else if (long.class.equals(field.getType()) || Long.class.equals(field.getType())) {
            preparedStatement.setLong(preparedStatementIndex++, (Long) field.get(t));
          } else if (String.class.equals(field.getType())) {
            preparedStatement.setString(preparedStatementIndex++, (String) field.get(t));
          } else if (boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType())) {
            preparedStatement.setBoolean(preparedStatementIndex++, (Boolean) field.get(t));
          } else if (double.class.equals(field.getType()) || Double.class.equals(field.getType())) {
            preparedStatement.setDouble(preparedStatementIndex++, (Double) field.get(t));
          } else if (float.class.equals(field.getType()) || Float.class.equals(field.getType())) {
            preparedStatement.setFloat(preparedStatementIndex++, (Float) field.get(t));
          } else {
            throw new IllegalStateException("Unexpected value: " + field.getType());
          }
        }

        int noOfRowsUpdated = preparedStatement.executeUpdate();
        System.out.println("Number of records inserted: " + noOfRowsUpdated);
        return t;
    }

    @Override
    public Collection<T> saveAll(Collection<T> items) throws Exception {
        ArrayList<T> result = new ArrayList<>();
        for (T item : items) {
            T save = save(item);
            result.add(save);
        }
        return result;
    }

    private void setPreparedStatement(Class<?> clss, PreparedStatement preparedStatement, int index, ID id) throws  Exception{
        if (int.class.equals(clss) || Integer.class.equals(clss)) {
            preparedStatement.setInt(index, (Integer) id);
        } else if (long.class.equals(clss) || Long.class.equals(clss)) {
            preparedStatement.setLong(index, (Long) id);
        } else if (String.class.equals(clss)) {
            preparedStatement.setString(index, (String) id);
        } else if (boolean.class.equals(clss) || Boolean.class.equals(clss)) {
            preparedStatement.setBoolean(index, (Boolean) id);
        } else if (double.class.equals(clss) || Double.class.equals(clss)) {
            preparedStatement.setDouble(index, (Double) id);
        } else if(float.class.equals(clss) || Float.class.equals(clss)){
            preparedStatement.setFloat(index, (Float) id);
        } else {
            throw new IllegalStateException("Unexpected value: " + clss);
        }
    }

    private void setFields(Field field, ResultSet resultSet, T t, String columnName) throws Exception{
        if (int.class.equals(field.getType()) || Integer.class.equals(field.getType())) {
            field.set(t, resultSet.getInt(columnName));
        } else if (long.class.equals(field.getType()) || Long.class.equals(field.getType())) {
            field.set(t, resultSet.getLong(columnName));
        } else if (String.class.equals(field.getType())) {
            field.set(t, resultSet.getString(columnName));
        } else if (boolean.class.equals(field.getType()) || Boolean.class.equals(field.getType())) {
            field.set(t, resultSet.getBoolean(columnName));
        } else if (double.class.equals(field.getType()) || Double.class.equals(field.getType())) {
            field.set(t, resultSet.getDouble(columnName));
        } else if(float.class.equals(field.getType()) || Float.class.equals(field.getType())){
            field.set(t, resultSet.getFloat(columnName));
        } else {
            throw new IllegalStateException("Unexpected value: " + field.getType());
        }
    }
}
