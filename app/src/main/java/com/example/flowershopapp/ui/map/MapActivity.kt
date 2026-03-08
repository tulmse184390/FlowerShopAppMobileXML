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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flowershopapp.R
import com.example.flowershopapp.data.model.StoreLocationDto
import com.example.flowershopapp.databinding.ActivityMapBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlin.math.roundToInt

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private val viewModel: MapViewModel by viewModels()
    private lateinit var storeAdapter: StoreLocationAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var googleMap: GoogleMap? = null
    private var selectedStore: StoreLocationDto? = null
    private var routePolyline: Polyline? = null
    private val allStores = mutableListOf<StoreLocationDto>()
    private val filteredStores = mutableListOf<StoreLocationDto>()
    private val markerMap = mutableMapOf<Int, Marker>()
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var pendingDirectionsStore: StoreLocationDto? = null

    private data class RouteResult(
        val path: List<LatLng>,
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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.fetchStores()
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

        // Tap on collapsed sheet / search bar -> expand to half
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
        // Focus on search also expands
        binding.edtStoreSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
    }

    private fun updateMapPadding() {
        val map = googleMap ?: return
        val sheetTop = binding.bottomSheet.top
        val parentHeight = binding.root.height
        val bottomPad = parentHeight - sheetTop
        map.setPadding(0, 0, 0, bottomPad.coerceAtLeast(0))
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isTiltGesturesEnabled = true

        requestLocationPermission()
        updateMapPadding()

        // Tap on map -> collapse the bottom sheet
        map.setOnMapClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        map.setOnMarkerClickListener { marker ->
            val store = marker.tag as? StoreLocationDto
            if (store != null) {
                focusOnStore(store)
            }
            true
        }

        // If stores already loaded before map was ready
        if (filteredStores.isNotEmpty()) {
            addStoreMarkers(filteredStores)
        } else {
            viewModel.stores.value?.let { stores ->
                if (stores.isNotEmpty()) {
                    allStores.clear()
                    allStores.addAll(stores)
                    applyStoreFilter(binding.edtStoreSearch.text?.toString().orEmpty())
                }
            }
        }
    }

    private fun addStoreMarkers(stores: List<StoreLocationDto>) {
        val map = googleMap ?: return
        map.clear()
        routePolyline = null
        markerMap.clear()

        if (stores.isEmpty()) {
            return
        }

        val boundsBuilder = LatLngBounds.Builder()

        for (store in stores) {
            val position = LatLng(store.latitude, store.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(store.storeName ?: "Store")
                    .snippet(store.address ?: "")
            )
            marker?.tag = store
            if (marker != null) {
                markerMap[store.locationId] = marker
            }
            boundsBuilder.include(position)
        }

        try {
            val bounds = boundsBuilder.build()
            val padding = 100
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        } catch (_: Exception) {
            // Single store or no stores - zoom to first
            if (stores.isNotEmpty()) {
                val first = stores[0]
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(first.latitude, first.longitude), 14f
                    )
                )
            }
        }
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

        val target = LatLng(store.latitude, store.longitude)
        val marker = markerMap[store.locationId]
        marker?.showInfoWindow()

        // Delay camera move slightly so map padding has updated after sheet state change
        binding.root.postDelayed({
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 15f))
        }, 150)
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
                    // lastLocation can be null; request a fresh fix
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
        val userLocation = LatLng(location.latitude, location.longitude)
        val route = findShortestPath(userLocation, targetShop)
        drawRoute(route)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        Toast.makeText(
            this,
            "Route: ${"%.2f".format(route.distanceKm)} km · ~${route.durationMinutes} min",
            Toast.LENGTH_LONG
        ).show()
    }

    // Calculates the shortest direct route between the user and a target shop.
    private fun findShortestPath(userLocation: LatLng, targetShop: StoreLocationDto): RouteResult {
        val target = LatLng(targetShop.latitude, targetShop.longitude)

        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude,
            userLocation.longitude,
            target.latitude,
            target.longitude,
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
        val map = googleMap ?: return
        routePolyline?.remove()

        routePolyline = map.addPolyline(
            PolylineOptions()
                .addAll(route.path)
                .color(android.graphics.Color.parseColor("#F4487D"))
                .width(12f)
                .geodesic(true)
        )

        val boundsBuilder = LatLngBounds.Builder()
        route.path.forEach { boundsBuilder.include(it) }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
    }

    private fun openDirections(store: StoreLocationDto) {
        val uri = Uri.parse("google.navigation:q=${store.latitude},${store.longitude}&mode=d")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback: open in browser
            val browserUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=${store.latitude},${store.longitude}&travelmode=driving"
            )
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
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
        val map = googleMap ?: return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
    }
}
