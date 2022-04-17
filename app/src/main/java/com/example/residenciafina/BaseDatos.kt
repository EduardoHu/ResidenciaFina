package com.example.residenciafina

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos(context: Context?,
                name: String?,
                factory: SQLiteDatabase.CursorFactory?,
                version: Int):SQLiteOpenHelper(context, name, factory, version)  {
    override fun onCreate(bd: SQLiteDatabase) {
        bd.execSQL("CREATE TABLE POSTES(IDPOSTE INTEGER PRIMARY KEY AUTOINCREMENT ,FECHA VARCHAR(100),POBLACION VARCHAR(100),COLONIA VARCHAR(100),TIPOPOSTE VARCHAR(100),ESTADOPOSTE VARCHAR(100))")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}