package com.example.flowershopapp.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flowershopapp.data.model.StoreLocationDto
import com.example.flowershopapp.databinding.ActivityMapBinding
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlin.math.roundToInt

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private val viewModel: MapViewModel by viewModels()
    private lateinit var storeAdapter: StoreLocationAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var isMapReady = false
    private var selectedStore: StoreLocationDto? = null
    private val allStores = mutableListOf<StoreLocationDto>()
    private val filteredStores = mutableListOf<StoreLocationDto>()
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var pendingDirectionsStore: StoreLocationDto? = null
    private var currentUserLocation: SimpleLatLng? = null
    private val gson = Gson()

    data class SimpleLatLng(val lat: Double, val lng: Double)

    private data class RouteResult(
        val path: List<SimpleLatLng>,
        val distanceKm: Double,
        val durationMinutes: Int
    )

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomSheet()
        setupUI()
        setupObservers()
        setupWebView()

        viewModel.fetchStores()
        requestLocationPermission()
    }

    private fun setupWebView() {
        binding.mapWebView.settings.javaScriptEnabled = true
        binding.mapWebView.settings.domStorageEnabled = true
        binding.mapWebView.addJavascriptInterface(WebAppInterface(), "Android")
        binding.mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                isMapReady = true
                updateMapPadding()
                // In case stores loaded before WebView finished
                if (filteredStores.isNotEmpty()) {
                    addStoreMarkers(filteredStores)
                } else if (viewModel.stores.value?.isNotEmpty() == true) {
                    allStores.clear()
                    allStores.addAll(viewModel.stores.value!!)
                    applyStoreFilter(binding.edtStoreSearch.text?.toString().orEmpty())
                }
            }
        }
        binding.mapWebView.loadUrl("file:///android_asset/leaflet_map.html")
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onMarkerClick(storeId: Int) {
            runOnUiThread {
                val store = allStores.find { it.locationId == storeId }
                if (store != null) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                    focusOnStore(store)
                }
            }
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.isFitToContents = false
        bottomSheetBehavior.halfExpandedRatio = 0.6f
        bottomSheetBehavior.expandedOffset = 0
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.skipCollapsed = false

        binding.root.post {
            val parentHeight = binding.root.height
            val minPeek = binding.searchBarContainer.height + binding.sheetHandleArea.height
            val tenPercentPeek = (parentHeight * 0.10f).roundToInt()
            bottomSheetBehavior.peekHeight = maxOf(tenPercentPeek, minPeek)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            updateMapPadding()
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                updateMapPadding()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                updateMapPadding()
            }
        })

        binding.searchBarContainer.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
        binding.sheetHandleArea.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
        binding.edtStoreSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
    }

    private fun updateMapPadding() {
        if (!isMapReady) return
        val sheetTop = binding.bottomSheet.top
        val parentHeight = binding.root.height
        val bottomPad = parentHeight - sheetTop
        val safeBottomPad = bottomPad.coerceAtLeast(0)
        
        binding.mapWebView.evaluateJavascript("setMapPadding($safeBottomPad);", null)
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        storeAdapter = StoreLocationAdapter(
            onStoreClick = { store ->
                focusOnStore(store)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            },
            onDirectionsClick = { store ->
                handleDirections(store)
            }
        )
        binding.rvStoreLocations.adapter = storeAdapter

        binding.edtStoreSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyStoreFilter(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.btnNavigate.setOnClickListener {
            val store = selectedStore ?: return@setOnClickListener
            openDirections(store)
        }

        binding.btnClearRoute.setOnClickListener {
            hideDirectionsCard()
        }
    }

    private fun setupObservers() {
        viewModel.stores.observe(this) { stores ->
            allStores.clear()
            allStores.addAll(stores)
            applyStoreFilter(binding.edtStoreSearch.text?.toString().orEmpty())
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addStoreMarkers(stores: List<StoreLocationDto>) {
        if (!isMapReady) return
        
        binding.cardDirectionsInfo.visibility = View.GONE
        currentUserLocation = null
        
        val json = gson.toJson(stores)
        val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
        binding.mapWebView.evaluateJavascript("addMarkers('$escapedJson');", null)
    }

    private fun applyStoreFilter(keyword: String) {
        val query = keyword.trim().lowercase()
        val result = if (query.isBlank()) {
            allStores
        } else {
            allStores.filter {
                (it.storeName ?: "").lowercase().contains(query) ||
                    (it.address ?: "").lowercase().contains(query)
            }
        }

        filteredStores.clear()
        filteredStores.addAll(result)

        storeAdapter.submitList(filteredStores)
        addStoreMarkers(filteredStores)

        if (filteredStores.isEmpty()) {
            selectedStore = null
            storeAdapter.selectStore(null)
            Toast.makeText(this, "No matching store found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun focusOnStore(store: StoreLocationDto) {
        selectedStore = store
        storeAdapter.selectStore(store.locationId)

        if (isMapReady) {
            binding.mapWebView.evaluateJavascript("focusOnStore(${store.latitude}, ${store.longitude}, ${store.locationId});", null)
        }
    }

    private fun handleDirections(targetShop: StoreLocationDto) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        pendingDirectionsStore = targetShop
        Toast.makeText(this, "Getting your location…", Toast.LENGTH_SHORT).show()

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onUserLocationObtained(location, targetShop)
                } else {
                    requestFreshLocation(targetShop)
                }
            }
            .addOnFailureListener {
                requestFreshLocation(targetShop)
            }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun requestFreshLocation(targetShop: StoreLocationDto) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()

        fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                val loc = result.lastLocation
                if (loc != null) {
                    onUserLocationObtained(loc, targetShop)
                } else {
                    Toast.makeText(this@MapActivity, "Could not get location", Toast.LENGTH_SHORT).show()
                    openDirections(targetShop)
                }
            }
        }, mainLooper)
    }

    private fun onUserLocationObtained(location: Location, targetShop: StoreLocationDto) {
        val userLocation = SimpleLatLng(location.latitude, location.longitude)
        currentUserLocation = userLocation

        if (isMapReady) {
            binding.mapWebView.evaluateJavascript("showUserLocation(${location.latitude}, ${location.longitude});", null)
        }

        val route = findShortestPath(userLocation, targetShop)
        drawRoute(route)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        focusOnStore(targetShop)
        showDirectionsCard(targetShop, route)
    }

    private fun showDirectionsCard(store: StoreLocationDto, route: RouteResult) {
        binding.tvDirectionsStoreName.text = store.storeName ?: "Store"
        binding.tvDirectionsDistance.text = "%.2f km".format(route.distanceKm)
        binding.tvDirectionsDuration.text = "~${route.durationMinutes} min"
        binding.cardDirectionsInfo.visibility = View.VISIBLE
    }

    private fun hideDirectionsCard() {
        binding.cardDirectionsInfo.visibility = View.GONE
        if (isMapReady) {
            binding.mapWebView.evaluateJavascript("clearRoute();", null)
        }
        currentUserLocation = null
    }

    private fun findShortestPath(userLocation: SimpleLatLng, targetShop: StoreLocationDto): RouteResult {
        val target = SimpleLatLng(targetShop.latitude, targetShop.longitude)

        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.lat,
            userLocation.lng,
            target.lat,
            target.lng,
            results
        )

        val distanceKm = results[0] / 1000.0
        val averageCitySpeedKmh = 30.0
        val durationMinutes = ((distanceKm / averageCitySpeedKmh) * 60.0)
            .roundToInt()
            .coerceAtLeast(1)

        return RouteResult(
            path = listOf(userLocation, target),
            distanceKm = distanceKm,
            durationMinutes = durationMinutes
        )
    }

    private fun drawRoute(route: RouteResult) {
        if (!isMapReady) return
        val json = gson.toJson(route.path)
        val escapedJson = json.replace("\\", "\\\\").replace("'", "\\'")
        binding.mapWebView.evaluateJavascript("drawRoute('$escapedJson');", null)
    }

    private fun openDirections(store: StoreLocationDto) {
        val userLoc = currentUserLocation

        val uri = if (userLoc != null) {
            Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                "&origin=${userLoc.lat},${userLoc.lng}" +
                "&destination=${store.latitude},${store.longitude}" +
                "&travelmode=driving"
            )
        } else {
            Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                "&destination=${store.latitude},${store.longitude}" +
                "&travelmode=driving"
            )
        }

        val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapsIntent.resolveActivity(packageManager) != null) {
            startActivity(mapsIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun enableMyLocation() {
        if (isMapReady) {
            // Leaflet user location tracking logic if needed
        }
    }
}
