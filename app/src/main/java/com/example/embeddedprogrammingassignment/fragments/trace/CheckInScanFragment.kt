package com.example.embeddedprogrammingassignment.fragments.trace

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.embeddedprogrammingassignment.MainActivity
import com.example.embeddedprogrammingassignment.R

private const val CAMERA_REQUEST_CODE = 101

class CheckInScanFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var tvResults: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_check_in_scan, container, false)
        setupPermission()
        codeScanner(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun codeScanner(view: View) {
        val codeScannerID = view.findViewById<CodeScannerView>(R.id.scanScanCode)
        tvResults = view.findViewById<TextView>(R.id.tvScanResult)

        codeScanner = CodeScanner(requireContext(), codeScannerID)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                activity?.runOnUiThread {
                    // Write code here
                    val result: String = it.text
                    tvResults.text = result
                    // if this is json file
                    if (result.contains("http://") || result.contains("www.") || result.contains("https://")) {
                        Log.i("Website", result)
                        codeScanner.stopPreview()
                        Navigation.findNavController(view).navigate(R.id.action_checkInScanFragment_to_checkInSuccessfulFragment)
                    }
                }
            }

            errorCallback = ErrorCallback {
                activity?.runOnUiThread {
                    Log.e("Main", "Camera initialization error: ${it.message}")
                }
            }
        }
        codeScannerID.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun setupPermission() {
        val permission: Int = ContextCompat.checkSelfPermission(requireContext(),
            android.Manifest.permission.CAMERA)
        if(permission != PackageManager.PERMISSION_GRANTED)
            makeRequest()
    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "You need the camera permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}