package com.example.residenciafina

import android.Manifest
import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.core.widget.addTextChangedListener
import com.example.residenciafina.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import org.apache.commons.math3.geometry.partitioning.Region
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    val PERMISSION_ID = 1010
    var baseDatos = BaseDatos(this,"POSTES",null,1)
    var listaID = ArrayList<String>()
    var datalista = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
       // setContentView(R.layout.activity_main)
        setContentView(view)
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), PackageManager.PERMISSION_GRANTED
        )
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.button4.setOnClickListener { consulta() }

        binding.button5.setOnClickListener {
            Log.d("Debug:",CheckPermission().toString())
            Log.d("Debug:",isLocationEnabled().toString())
            RequestPermission()
            /* fusedLocationProviderClient.lastLocation.addOnSuccessListener{location: Location? ->
                 textView.text = location?.latitude.toString() + "," + location?.longitude.toString()
             }*/
            getLastLocation()
        }
        binding.button6.setOnClickListener { generarexcel() }
        cargarpostes()
    }

    /*fun crearexcelya(){
        val mQuiz = Quiz()
        mQuiz.quizName = "Excel-quiz"
        val question1 = Question("Where do you find the best answers?", "Stack-Overflow")
        val question2 = Question("Who to ask", "mwb")
        val workbook =  XSSFWorkbook()
        val creationHelper = workbook.creationHelper
        val sheet = workbook.createSheet("Quiz")
        val row1 = sheet.createRow(0)
        val row2 = sheet.createRow(1)
        row1.createCell(0).setCellValue("Quiz")
        var col:Int=1
        for (question in mQuiz.questions) {
            row1.createCell(col).setCellValue("Question $col")
            row2.createCell(col).setCellValue(question.question)
            col++
        }

        try {
            val file= FileOutputStream("quiz.xlsx")
            workbook.write(file)
            file.close()
        }catch (e:FileNotFoundException){
            e.printStackTrace()
        }catch (e:IOException){
            e.printStackTrace()
        }
    }*/

    /*private fun excelfregon(){
       /* val xlwb=XSSFWorkbook()
        val xlWs=xlwb.createSheet()
        xlWs.createRow(0).createCell(0).setCellValue("Hello Excel")
        val output =FileOutputStream("./test.xlsx")
        xlwb.write(output)
        xlwb.close()*/
        try{
            val file =File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/Demo.xlsx")
            val xlwb=XSSFWorkbook()
            val xlWs=xlwb.createSheet()
            /*Header "Sampe..."*/
            xlWs.createRow(0).createCell(0).setCellValue("Hello Excel")
            if (!file.exists()){
                file.createNewFile()
            }
            val fileOutputStream = FileOutputStream(file)
            xlwb.write(fileOutputStream)
            if (fileOutputStream!=null){
                fileOutputStream.flush()
                fileOutputStream.close()
            }

        }catch (e:Exception){
            e.printStackTrace()
        }

    }*/

    private fun generarexcel(){
        try{

            //val file =File(Environment.getExternalStorageDirectory().toString() + "/Demo.xls")
            var ruta = getExternalFilesDir(null)!!.absolutePath
            var archivoSD = File(ruta, "" + "/Demo.xls")
            val workbook = HSSFWorkbook()
            val spreadSheet = workbook.createSheet("POSTESCFE")
            val rowA = spreadSheet.createRow(0)
            val cellAA = rowA.createCell(0)
            cellAA.setCellValue(HSSFRichTextString("IdPoste"))
            val cellBB = rowA.createCell(1)
            cellBB.setCellValue(HSSFRichTextString("Fecha"))
            val cellC = rowA.createCell(2)
            cellC.setCellValue(HSSFRichTextString("Poblacion"))
            val cellD = rowA.createCell(3)
            cellD.setCellValue(HSSFRichTextString("Colonia"))
            val cellE = rowA.createCell(4)
            cellE.setCellValue(HSSFRichTextString("Tipo de Poste"))
            val cellF = rowA.createCell(5)
            cellF.setCellValue(HSSFRichTextString("Estado del Poste"))
            spreadSheet.setColumnWidth(0,(20*200))
            spreadSheet.setColumnWidth(1,(20*200))
            spreadSheet.setColumnWidth(2,(20*200))
            spreadSheet.setColumnWidth(3,(20*200))
            spreadSheet.setColumnWidth(4,(20*200))
            spreadSheet.setColumnWidth(5,(20*200))


            //----------------------------------------------------------------//
            //          Evaluar Postes                                         //
            //----------------------------------------------------------------//


            val rowB = spreadSheet.createRow(3)
            val cellAB = rowB.createCell(7)
            cellAB.setCellValue(HSSFRichTextString("Total de postes funcionando"))
            val cellAC = rowB.createCell(8)
            cellAC.setCellValue(HSSFRichTextString("Total de postes no funcionando"))
            spreadSheet.setColumnWidth(7,(20*400))
            spreadSheet.setColumnWidth(8,(20*400))
            //--------------------------------------------------------------//

            try{
                var select = baseDatos.readableDatabase
                var SQL = "SELECT * FROM POSTES WHERE ESTADOPOSTE ='FUNCIONA'"
                var SQL2 = "SELECT * FROM POSTES WHERE ESTADOPOSTE ='NO FUNCIONA'"
                val cursor=select.rawQuery(SQL, null)
                val cursor2=select.rawQuery(SQL2,null)
               val n:Int=cursor.count
                val rowC = spreadSheet.createRow(4)
                val cellfeliz = rowC.createCell(7)
                cellfeliz.setCellValue(HSSFRichTextString(cursor.count.toString()))
                val cellfeliz2 = rowC.createCell(8)
                cellfeliz2.setCellValue(HSSFRichTextString(cursor2.count.toString()))
                spreadSheet.setColumnWidth(7,(20*400))
                spreadSheet.setColumnWidth(8,(20*400))
                select.close()

            }catch (err:SQLiteException){
                mensaje(err.message!!)
            }

            if (!archivoSD.exists()){
                archivoSD.createNewFile()
            }
            val fileOutputStream = FileOutputStream(archivoSD)
            workbook.write(fileOutputStream)
            if (fileOutputStream!=null){
                fileOutputStream.flush()
                fileOutputStream.close()
            }

            try{
                val uri= Uri.fromFile(archivoSD)
                val i =Intent(Intent.ACTION_SEND)
                //i.setType("application/xls")
                //i.type = "vnd.android.cursor.dir/email"
                i.setType("text/*")
                i.putExtra(Intent.EXTRA_EMAIL, arrayListOf("eduardohdez96@gmail.com" ))
                i.putExtra(Intent.EXTRA_SUBJECT, "Envio de archivo XLS.")
                i.putExtra(Intent.EXTRA_TEXT, "Hola te envío un archivo XLS.")
                i.putExtra(Intent.EXTRA_STREAM,  uri)
                startActivity(Intent.createChooser(i, "Enviar email......"))
                finish()
            }catch (ex:android.content.ActivityNotFoundException){
                Toast.makeText(this, "No tienes clientes de email instalados.", Toast.LENGTH_SHORT).show()
            }




        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    fun CheckPermission():Boolean{
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if(
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false

    }

    fun RequestPermission(){
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    fun getLastLocation(){
        if(CheckPermission()){
            if(isLocationEnabled()){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                    var location: Location? = task.result
                    if(location == null){
                        NewLocationData()
                    }else{
                        Log.d("Debug:" ,"Your Location:"+ location.longitude)
                        /*binding.textView2.text = "You Current Location is : Long: "+ location.longitude + " , Lat: " + location.latitude + "\n" + getCityName(location.latitude,location.longitude)+"\n" +
                                "Colonia: "+getColonia(location.latitude,location.longitude)*/
                        try {
                            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                            val currentDate = sdf.format(Date())

                            var insertar = baseDatos.writableDatabase
                            var SQL ="INSERT INTO POSTES VALUES(NULL,'${currentDate.toString()}','${getCityName(location.latitude,location.longitude).toString()}','${getColonia(location.latitude,location.longitude).toString()}','${binding.tipoposte.text.toString()}','${"FUNCIONA"}')"
                            insertar.execSQL(SQL)
                            cargarpostes()
                            limpiarcampos()
                            insertar.close()
                        }catch (err: SQLiteException){
                            mensaje(err.message!!)
                        }



                    }
                }
            }else{
                Toast.makeText(this,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }
    }

    private fun limpiarcampos() {
        binding.tipoposte.setText("")
    }

   /* private fun enviarmensaje(){
        try{
            val filename = "Demo.xls"
            val filelocation = File(Environment.getExternalStorageDirectory().absolutePath, filename)
            val path = Uri.fromFile(filelocation)
            val i =Intent(Intent.ACTION_SEND)
            i.type = "vnd.android.cursor.dir/email"
            i.putExtra(Intent.EXTRA_EMAIL, arrayListOf("eduardohdez96@gmail.com" ))
            i.putExtra(Intent.EXTRA_SUBJECT, "Envio de archivo XLS.")
            i.putExtra(Intent.EXTRA_TEXT, "Hola te envío un archivo XLS.")
            i.putExtra(Intent.EXTRA_STREAM, path)
            startActivity(Intent.createChooser(i, "Enviar email......"))
            finish()
        }catch (ex:android.content.ActivityNotFoundException){
            Toast.makeText(this, "No tienes clientes de email instalados.", Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun cargarpostes() {
        try {
            var select = baseDatos.readableDatabase
            var postes = ArrayList<String>()
            var SQL = "SELECT * FROM POSTES"

            var cursor =select.rawQuery(SQL,null)
            listaID.clear()

            if(cursor.moveToFirst()){
                do{
                    var data = cursor.getInt(0).toString()+" "+"["+cursor.getString(1)+"] -- "+"["+ cursor.getString(2) +"]-- "+"["+ cursor.getString(3)+"]--"+"["+cursor.getString(4)+"]--"+"["+cursor.getString(5)+"]"
                    postes.add(data)
                    listaID.add(cursor.getInt(0).toString())

                }while (cursor.moveToNext())

            }else{
                postes.add("NO HAY POSTES INGRESADOS")
            }
            select.close()

            /*binding.listpostes.adapter = ArrayAdapter<String>(this,
                R.layout.simple_expandable_list_item_1,postes)*/

            val adapter = ArrayAdapter<String>(this,
                R.layout.simple_expandable_list_item_1,postes)
            binding.listpostes.adapter=adapter

           /* binding.idposte.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
                override fun onQueryTextChange(newText: String?): Boolean {

                    binding.idposte.clearFocus()
                    if (postes.contains(newText)){
                        adapter.filter.filter(newText)
                    }
                    else{
                        Toast.makeText(applicationContext,"no hay ",Toast.LENGTH_LONG).show()
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    adapter.filter.filter(query)
                    return false
                }


            })*/


            binding.listpostes.setOnItemClickListener {
                    adapterView, view, posicion, l ->
                var idBorrar = listaID.get(posicion)
                AlertDialog.Builder(this)
                    .setTitle("ATENCION")
                    .setMessage("¿QUE DESEAS HACER CON ID: ${idBorrar}")
                    .setNegativeButton("CANCELAR"){d,i->
                        d.dismiss()
                    }
                    .setPositiveButton("Eliminar"){d,i->
                        eliminar(idBorrar)
                    }
                    .setNeutralButton("ACTUALIZAR"){d,i->
                        /*var intent = Intent(this,MainActivity2::class.java)
                        intent.putExtra("idactualizar",idBorrar)
                        startActivity(intent)*/
                        d.dismiss()
                    }
                    .show()
            }

        }catch (err:SQLiteException){
            mensaje(err.message!!)
        }

    }

    private fun eliminar(idBorrar: String) {

        try {
            var eliminar = baseDatos.writableDatabase
            var SQL ="DELETE FROM POSTES WHERE IDPOSTE =${idBorrar}"
            eliminar.execSQL(SQL)
            cargarpostes()
            eliminar.close()
        }catch (err:SQLiteException){
            mensaje(err.message!!)
        }

    }

    private fun consulta(){
        if(binding.idposte.text.isEmpty()){
            mensaje("ID VACIO, FAVOR DE INGRESAR UN ID")
        }
        else{
            try {
                var select = baseDatos.readableDatabase
                var postes = ArrayList<String>()
                var SQL = "SELECT * FROM POSTES WHERE IDPOSTE = "+binding.idposte.text

                var cursor =select.rawQuery(SQL,null)
                listaID.clear()

                if(cursor.moveToFirst()){
                    do{
                        var data = cursor.getInt(0).toString()+" "+"["+cursor.getString(1)+"] -- "+"["+ cursor.getString(2) +"]-- "+"["+ cursor.getString(3)+"]--"+"["+cursor.getString(4)+"]--"+"["+cursor.getString(5)+"]"
                        postes.add(data)
                        listaID.add(cursor.getInt(0).toString())

                    }while (cursor.moveToNext())

                }else{
                    postes.add("NO HAY POSTES INGRESADOS")
                }
                select.close()

                binding.listpostes.adapter = ArrayAdapter<String>(this,
                    R.layout.simple_expandable_list_item_1,postes)



                binding.listpostes.setOnItemClickListener {
                        adapterView, view, posicion, l ->
                    var idBorrar = listaID.get(posicion)
                    AlertDialog.Builder(this)
                        .setTitle("ATENCION")
                        .setMessage("¿QUE DESEAS HACER CON ID: ${idBorrar}")
                        .setNegativeButton("CANCELAR"){d,i->
                            d.dismiss()
                        }
                        .setPositiveButton("Eliminar"){d,i->
                            eliminar(idBorrar)
                        }
                        .setNeutralButton("ACTUALIZAR"){d,i->
                            /*var intent = Intent(this,MainActivity2::class.java)
                            intent.putExtra("idactualizar",idBorrar)
                            startActivity(intent)*/
                            d.dismiss()
                        }
                        .show()
                }

            }catch (err:SQLiteException){
                mensaje(err.message!!)
            }

        }
    }

    private fun getCityName(lat: Double, long: Double): String{
        var cityName:String = ""
        var countryName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat,long,3)

        cityName = Adress.get(0).locality
        countryName = Adress.get(0).countryName
        Log.d("Debug:","Your City: " + cityName + " ; your Country " + countryName)
        return cityName
    }
    private fun getColonia(lat: Double, long: Double): String{
        var coloniaName:String = ""
        var countryName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat,long,3)

        coloniaName = Adress.get(0).subLocality
        countryName = Adress.get(0).countryName
        Log.d("Debug:","Your City: " + coloniaName + " ; your Country " + countryName)
        return coloniaName
    }



    private fun NewLocationData() {
        var locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )
    }
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d("Debug:","your last last location: "+ lastLocation.longitude.toString())
           /* binding.textView2.text = "You Last Location is : Long: "+ lastLocation.longitude + " , Lat: " + lastLocation.latitude + "\n" + getCityName(lastLocation.latitude,lastLocation.longitude)+
                    "\n"+"Colonia: "+getColonia(lastLocation.latitude,lastLocation.longitude)*/
        }
        
    }

    fun isLocationEnabled():Boolean{
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Debug:","You have the Permission")
            }
        }




    }


    private fun mensaje(s: String) {
        AlertDialog.Builder(this).setTitle("Atencion").setMessage(s).setPositiveButton("OK"){ d, i->}.show()
    }



}

