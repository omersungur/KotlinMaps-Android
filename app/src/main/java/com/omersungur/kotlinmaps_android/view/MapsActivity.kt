package com.omersungur.kotlinmaps_android.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.room.Room

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.omersungur.kotlinmaps_android.R
import com.omersungur.kotlinmaps_android.databinding.ActivityMapsBinding
import com.omersungur.kotlinmaps_android.model.Place
import com.omersungur.kotlinmaps_android.roomdb.PlaceDAO
import com.omersungur.kotlinmaps_android.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager // Lokasyonla ilgili işlemlerin gerçekleştirmemize yardımcı olur.
    private lateinit var locationListener: LocationListener // Konumda gerçekten bir değişiklik oldu mu onu kontrol eder.
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackLocation : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var db : PlaceDatabase
    private lateinit var placeDao : PlaceDAO
    val compositeDisposible = CompositeDisposable() // İşlem yaptıkça hafızda yer tutarız, bunları silmek için oluşturuyoruz.
    var placeFromMain : Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sharedPreferences = this.getSharedPreferences("com.omersungur.kotlinmaps_android", MODE_PRIVATE)
        trackLocation = false
        selectedLatitude = 0.0
        selectedLongitude = 0.0

        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
            //.allowMainThreadQueries() RxJava kullanmadan direkt UI içinde veri tabanı işlemlerini yapabiliriz ama kötü bir yöntem.
            .build()
        placeDao = db.placeDao()

        binding.saveButton.isEnabled = false

        registerLauncher()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        // Latitude - Enlem, Longitude - Boylam

        /*val eiffel = LatLng(48.858093,2.294694)
        mMap.addMarker(MarkerOptions().position(eiffel).title("Eiffel Tower")) // verdiğmiz pozisyona marker koyduk.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel,15f))*/ // program açılınca ilk görüntüyü oraya zoomladık.

        intent = intent
        val info = intent.getStringExtra("Info")

        if(info.equals("NewData")) {

            binding.saveButton.isEnabled = true
            binding.deleteButton.visibility = View.GONE
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {

                    trackLocation = sharedPreferences.getBoolean("trackBoolean",false)

                    if(!trackLocation!!) {
                        val userLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                        //haritada gezinme yaptığımızda bir daha bu if bloğu çalışmayacak.
                    }
                }
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.root, "Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if(lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    //GPS tarafından elimizde bir son konum bulunuyorsa orayı gösteriyoruz.
                }
                mMap.isMyLocationEnabled = true
            }
        }
        else {
            binding.editText.isFocusable = false
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("SelectedPlace") as Place?

            placeFromMain?.let {
                val latlng = LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latlng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15f))

                binding.editText.setText(it.name)
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility= View.VISIBLE
            }
        }
    }
    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                        val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if(lastLocation != null) {
                            val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                        }
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(this@MapsActivity, "Permission denied", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onMapLongClick(p0: LatLng) {

        mMap.clear() // önceden koyulan işaretleri temizliyoruz.
        mMap.addMarker(MarkerOptions().position(p0))
        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude
        binding.saveButton.isEnabled = true
    }

    fun save (view : View) {

        // Main Thread UI> Kullanıcı arayüzünü ilgilendiren işlemleri yaptığımız threaddir.
        // Default Thread > Yoğun işlemleri yapabildiğimiz thread. Örneğin birçok sayıyı sıralama
        // I/O > İnternet / Veri tabanı işlemlerinin yapıldığı threaddir.

        if(selectedLatitude != null && selectedLongitude != null) {
            val place = Place(binding.editText.text.toString(), selectedLatitude!!, selectedLongitude!!)
            compositeDisposible.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())// İşlemin gerçekleştiği thread
                .observeOn(AndroidSchedulers.mainThread()) // İşlemin görüntülendiği thread
                .subscribe(this::handleResponse)) //this::handleResponse > bu işlem bitince handleResponse çalışsın demiş olduk.
        }
    }

    private fun handleResponse() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun delete(view : View) {
        placeFromMain?.let {
            compositeDisposible.add(placeDao.delete(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposible.clear()
    }
}