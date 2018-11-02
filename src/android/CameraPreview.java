package com.cordovaplugincamerapreview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.Arrays;

import android.widget.RelativeLayout;
import android.widget.FrameLayout;

public class CameraPreview extends CordovaPlugin implements CameraActivity.CameraPreviewListener {

  private static final String TAG = "PP/CameraPreview";

  private static final String COLOR_EFFECT_ACTION = "setColorEffect";
  private static final String SUPPORTED_COLOR_EFFECTS_ACTION = "getSupportedColorEffects";
  private static final String ZOOM_ACTION = "setZoom";
  private static final String GET_ZOOM_ACTION = "getZoom";
  private static final String GET_HFOV_ACTION = "getHorizontalFOV";
  private static final String GET_MAX_ZOOM_ACTION = "getMaxZoom";
  private static final String SUPPORTED_FLASH_MODES_ACTION = "getSupportedFlashModes";
  private static final String GET_FLASH_MODE_ACTION = "getFlashMode";
  private static final String SET_FLASH_MODE_ACTION = "setFlashMode";
  private static final String START_CAMERA_ACTION = "startCamera";
  private static final String STOP_CAMERA_ACTION = "stopCamera";
  private static final String PICTURE_SIZE_ACTION = "setPictureSize";
  private static final String SWITCH_CAMERA_ACTION = "switchCamera";
  private static final String TAKE_PICTURE_ACTION = "takePicture";
  private static final String SHOW_CAMERA_ACTION = "showCamera";
  private static final String HIDE_CAMERA_ACTION = "hideCamera";
  private static final String TAP_TO_FOCUS = "tapToFocus";
  private static final String SUPPORTED_PICTURE_SIZES_ACTION = "getSupportedPictureSizes";
  private static final String SUPPORTED_FOCUS_MODES_ACTION = "getSupportedFocusModes";
  private static final String SUPPORTED_WHITE_BALANCE_MODES_ACTION = "getSupportedWhiteBalanceModes";
  private static final String GET_FOCUS_MODE_ACTION = "getFocusMode";
  private static final String SET_FOCUS_MODE_ACTION = "setFocusMode";
  private static final String GET_EXPOSURE_MODES_ACTION = "getExposureModes";
  private static final String GET_EXPOSURE_MODE_ACTION = "getExposureMode";
  private static final String SET_EXPOSURE_MODE_ACTION = "setExposureMode";
  private static final String GET_EXPOSURE_COMPENSATION_ACTION = "getExposureCompensation";
  private static final String SET_EXPOSURE_COMPENSATION_ACTION = "setExposureCompensation";
  private static final String GET_EXPOSURE_COMPENSATION_RANGE_ACTION = "getExposureCompensationRange";
  private static final String GET_WHITE_BALANCE_MODE_ACTION = "getWhiteBalanceMode";
  private static final String SET_WHITE_BALANCE_MODE_ACTION = "setWhiteBalanceMode";
  private static final String SET_SCREEN_ROTATION_ACTION = "setScreenRotation";
  private static final String SET_BACK_BUTTON_CALLBACK = "onBackButton";

  private static final int CAM_REQ_CODE = 0;

  private static final String[] permissions = { Manifest.permission.CAMERA };

  private CameraActivity fragment;

  private CallbackContext takePictureCallbackContext;
  private CallbackContext setFocusCallbackContext;
  private CallbackContext startCameraCallbackContext;
  private CallbackContext tapBackButtonContext = null;

  private CallbackContext execCallback;
  private JSONArray execArgs;

  private ViewParent webViewParent;

  private int containerViewId = 20; // <- set to random number to prevent conflict with other plugins

  private int screenRotation = 0;

  public CameraPreview() {
    super();
    Log.d(TAG, "Constructing");
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    switch (action) {
    case START_CAMERA_ACTION:
      if (cordova.hasPermission(permissions[0])) {
        startCamera(args.getInt(0), args.getInt(1), args.getInt(2), args.getInt(3), args.getString(4),
            args.getBoolean(5), args.getBoolean(6), args.getBoolean(7), args.getString(8), args.getBoolean(9),
            args.getBoolean(10), args.getBoolean(11), callbackContext);
      } else {
        this.execCallback = callbackContext;
        this.execArgs = args;
        cordova.requestPermissions(this, CAM_REQ_CODE, permissions);
      }
      break;
    case TAKE_PICTURE_ACTION:
      takePicture(args.getInt(0), callbackContext);
      break;
    case COLOR_EFFECT_ACTION:
      setColorEffect(args.getString(0), callbackContext);
      break;
    case ZOOM_ACTION:
      setZoom(args.getDouble(0), callbackContext);
      break;
    case GET_ZOOM_ACTION:
      getZoom(callbackContext);
    case GET_HFOV_ACTION:
      getHorizontalFOV(callbackContext);
      break;
    case GET_MAX_ZOOM_ACTION:
      getMaxZoom(callbackContext);
      break;
    case PICTURE_SIZE_ACTION:
      setPictureSize(args.getInt(0), args.getInt(1), callbackContext);
      break;
    case SUPPORTED_FLASH_MODES_ACTION:
      getSupportedFlashModes(callbackContext);
      break;
    case GET_FLASH_MODE_ACTION:
      getFlashMode(callbackContext);
      break;
    case SET_FLASH_MODE_ACTION:
      setFlashMode(args.getString(0), callbackContext);
      break;
    case STOP_CAMERA_ACTION:
      stopCamera(callbackContext);
      break;
    case SHOW_CAMERA_ACTION:
      showCamera(callbackContext);
      break;
    case HIDE_CAMERA_ACTION:
      hideCamera(callbackContext);
      break;
    case TAP_TO_FOCUS:
      tapToFocus(args.getInt(0), args.getInt(1), callbackContext);
      break;
    case SWITCH_CAMERA_ACTION:
      switchCamera(callbackContext);
      break;
    case SUPPORTED_PICTURE_SIZES_ACTION:
      getSupportedPictureSizes(callbackContext);
      break;
    case GET_EXPOSURE_MODES_ACTION:
      getExposureModes(callbackContext);
      break;
    case SUPPORTED_FOCUS_MODES_ACTION:
      getSupportedFocusModes(callbackContext);
      break;
    case GET_FOCUS_MODE_ACTION:
      getFocusMode(callbackContext);
      break;
    case SET_FOCUS_MODE_ACTION:
      setFocusMode(args.getString(0), callbackContext);
      break;
    case GET_EXPOSURE_MODE_ACTION:
      getExposureMode(callbackContext);
      break;
    case SET_EXPOSURE_MODE_ACTION:
      setExposureMode(args.getString(0), callbackContext);
      break;
    case GET_EXPOSURE_COMPENSATION_ACTION:
      getExposureCompensation(callbackContext);
      break;
    case SET_EXPOSURE_COMPENSATION_ACTION:
      setExposureCompensation(args.getInt(0), callbackContext);
      break;
    case GET_EXPOSURE_COMPENSATION_RANGE_ACTION:
      getExposureCompensationRange(callbackContext);
      break;
    case SUPPORTED_WHITE_BALANCE_MODES_ACTION:
      getSupportedWhiteBalanceModes(callbackContext);
      break;
    case GET_WHITE_BALANCE_MODE_ACTION:
      getWhiteBalanceMode(callbackContext);
      break;
    case SET_WHITE_BALANCE_MODE_ACTION:
      setWhiteBalanceMode(args.getString(0), callbackContext);
      break;
    case SET_SCREEN_ROTATION_ACTION:
      setScreenRotation(args.getInt(0), callbackContext);
      break;
    case SET_BACK_BUTTON_CALLBACK:
      setBackButtonListener(callbackContext);
      break;
    case SUPPORTED_COLOR_EFFECTS_ACTION:
      getSupportedColorEffects(callbackContext);
    default:
      return false;
    }
    return true;
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
      throws JSONException {
    for (int r : grantResults) {
      if (r == PackageManager.PERMISSION_DENIED) {
        execCallback.sendPluginResult(new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION));
        return;
      }
    }
    if (requestCode == CAM_REQ_CODE) {
      startCamera(this.execArgs.getInt(0), this.execArgs.getInt(1), this.execArgs.getInt(2), this.execArgs.getInt(3),
          this.execArgs.getString(4), this.execArgs.getBoolean(5), this.execArgs.getBoolean(6),
          this.execArgs.getBoolean(7), this.execArgs.getString(8), this.execArgs.getBoolean(9),
          this.execArgs.getBoolean(10), this.execArgs.getBoolean(11), this.execCallback);
    }
  }

  private boolean hasView(CallbackContext callbackContext) {
    if (fragment == null) {
      callbackContext.error("No preview");
      return false;
    }

    return true;
  }

  private boolean hasCamera(CallbackContext callbackContext) {
    if (!this.hasView(callbackContext)) {
      return false;
    }

    if (fragment.getCamera() == null) {
      callbackContext.error("No Camera");
      return false;
    }

    return true;
  }

  private Camera getCameraInstance(int id) {

    Camera camera = null;

    try {
      camera = Camera.open(id);
    } catch (Exception e) {
      Log.e("BeMyEyeCamera", "Err: Camera unavailable : " + e.toString());
    }
    return camera;
  }

  private void getSupportedPictureSizes(CallbackContext callbackContext) {
    List<Camera.Size> supportedSizes = null;

    // TODO: change whole logic so that every method can be called even when camera
    // is closed
    if (fragment != null && fragment.getCamera() != null) {
      Camera camera = fragment.getCamera();
      supportedSizes = camera.getParameters().getSupportedPictureSizes();
    } else {
      Camera camera = this.getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
      if (camera != null) {
        supportedSizes = camera.getParameters().getSupportedPictureSizes();
        camera.release();
      }
    }

    if (supportedSizes == null) {
      callbackContext.error("Camera Parameters access error");
      return;
    }

    JSONArray sizes = new JSONArray();
    for (int i = 0; i < supportedSizes.size(); i++) {
      Camera.Size size = supportedSizes.get(i);
      int h = size.height;
      int w = size.width;
      JSONObject jsonSize = new JSONObject();
      try {
        jsonSize.put("height", new Integer(h));
        jsonSize.put("width", new Integer(w));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      sizes.put(jsonSize);
    }
    callbackContext.success(sizes);
  }

  private void startCamera(int x, int y, int width, int height, String defaultCamera, Boolean tapToTakePicture,
      Boolean dragEnabled, final Boolean toBack, String alpha, boolean tapFocus, boolean disableExifHeaderStripping, boolean businessCardOverlay,
      CallbackContext callbackContext) {
    Log.d(TAG, "start camera action");
    if (fragment != null) {
      callbackContext.error("CameraAlreadyStarted");
      return;
    }

    final float opacity = Float.parseFloat(alpha);

    fragment = new CameraActivity();
    fragment.setEventListener(this);
    fragment.defaultCamera = defaultCamera;
    fragment.tapToTakePicture = tapToTakePicture;
    fragment.dragEnabled = dragEnabled;
    fragment.tapToFocus = tapFocus;
    fragment.disableExifHeaderStripping = disableExifHeaderStripping;
    fragment.toBack = toBack;
    fragment.businessCardOverlay = businessCardOverlay;

    DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
    // offset
    final int computedX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, metrics);
    final int computedY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y, metrics);

    // size
    final int computedWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, metrics);
    final int computedHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics);

    startCameraCallbackContext = callbackContext;

    cordova.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {

        // The layout is only created on first call, and next times we reuse it
        RelativeLayout containerView = cordova.getActivity().findViewById(containerViewId);
        if (containerView == null) {
          containerView = new RelativeLayout(cordova.getActivity().getApplicationContext());
          containerView.setId(containerViewId);

          containerView.setGravity(Gravity.CENTER);
          RelativeLayout.LayoutParams containerLayoutParams = new RelativeLayout.LayoutParams(computedWidth,
              computedHeight);
          containerLayoutParams.setMargins(computedX, computedY, 0, 0);
          // Get the parent view of the webview
          ViewGroup webViewParentGroup = (ViewGroup) webView.getView().getParent();

          // Add the containerView to the parent of the webview
          webViewParentGroup.addView(containerView, containerLayoutParams);
        }

        containerView.setBackgroundColor(Color.BLACK);

        // display camera bellow the webview
        if (toBack) {
          webView.getView().setBackgroundColor(0x00000000);
          webViewParent = webView.getView().getParent();
          webView.getView().bringToFront();

        } else {

          // set camera back to front
          containerView.setAlpha(opacity);
          containerView.bringToFront();

        }

        if (businessCardOverlay) {
          final int margin = (int)(computedWidth * 0.3);
          final int extraBottomMargin = (int)(computedHeight * 0.2);
          final int imageWidth = computedWidth - margin;
          final int imageHeight = computedHeight - margin - extraBottomMargin;
          final ImageView imageView = new ImageView(cordova.getActivity().getApplicationContext());
          final int resourceId = cordova.getActivity().getResources().getIdentifier("bc_template_1_7", "drawable", cordova.getActivity().getPackageName());
          imageView.setImageResource(resourceId);
          imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
          FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams(imageWidth, imageHeight);
          imageLayoutParams.setMargins(computedX + (margin / 2), computedY + (margin / 2) + 64, 0, 0);
          ViewGroup webViewParentGroup = (ViewGroup) webView.getView().getParent();
          webViewParentGroup.addView(imageView, 1, imageLayoutParams);
          int index = webViewParentGroup.indexOfChild(imageView);
        }

        // add the fragment to the container
        FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(containerView.getId(), fragment);
        fragmentTransaction.commit();
      }
    });

  }

  public void onCameraStarted() {
    Log.d(TAG, "Camera started");
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "Camera started");
    pluginResult.setKeepCallback(true);
    startCameraCallbackContext.sendPluginResult(pluginResult);
  }

  private void takePicture(int quality, CallbackContext callbackContext) {
    if (!this.hasView(callbackContext)) {
      return;
    }

    takePictureCallbackContext = callbackContext;

    fragment.takePicture(quality);
  }

  public void onPictureTaken(String originalPicture) {
    Log.d(TAG, "returning picture");

    JSONArray data = new JSONArray();
    data.put(originalPicture);

    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
    pluginResult.setKeepCallback(true);
    takePictureCallbackContext.sendPluginResult(pluginResult);
  }

  public void onPictureTakenError(String message) {
    Log.d(TAG, "CameraPreview onPictureTakenError");
    takePictureCallbackContext.error(message);
  }

  private void setColorEffect(String effect, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    List<String> supportedColors;
    supportedColors = params.getSupportedColorEffects();

    if (supportedColors.contains(effect)) {
      params.setColorEffect(effect);
      fragment.setCameraParameters(params);
      callbackContext.success(effect);
    } else {
      callbackContext.error("Color effect not supported" + effect);
    }
  }

  private void getSupportedColorEffects(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();
    List<String> supportedColors;
    supportedColors = params.getSupportedColorEffects();
    JSONArray jsonColorEffects = new JSONArray();

    if (supportedColors != null) {
      for (int i = 0; i < supportedColors.size(); i++) {
        jsonColorEffects.put(new String(supportedColors.get(i)));
      }
    }

    callbackContext.success(jsonColorEffects);
  }

  private void getExposureModes(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    if (camera.getParameters().isAutoExposureLockSupported()) {
      JSONArray jsonExposureModes = new JSONArray();
      jsonExposureModes.put(new String("lock"));
      jsonExposureModes.put(new String("continuous"));
      callbackContext.success(jsonExposureModes);
    } else {
      callbackContext.error("Exposure modes not supported");
    }
  }

  private void getExposureMode(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    String exposureMode;

    if (camera.getParameters().isAutoExposureLockSupported()) {
      if (camera.getParameters().getAutoExposureLock()) {
        exposureMode = "lock";
      } else {
        exposureMode = "continuous";
      }
      ;
      callbackContext.success(exposureMode);
    } else {
      callbackContext.error("Exposure mode not supported");
    }
  }

  private void setExposureMode(String exposureMode, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    if (camera.getParameters().isAutoExposureLockSupported()) {
      params.setAutoExposureLock("lock".equals(exposureMode));
      fragment.setCameraParameters(params);
      callbackContext.success();
    } else {
      callbackContext.error("Exposure mode not supported");
    }
  }

  private void getExposureCompensation(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    if (camera.getParameters().getMinExposureCompensation() == 0
        && camera.getParameters().getMaxExposureCompensation() == 0) {
      callbackContext.error("Exposure corection not supported");
    } else {
      int exposureCompensation = camera.getParameters().getExposureCompensation();
      callbackContext.success(exposureCompensation);
    }
  }

  private void setExposureCompensation(int exposureCompensation, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    int minExposureCompensation = camera.getParameters().getMinExposureCompensation();
    int maxExposureCompensation = camera.getParameters().getMaxExposureCompensation();

    if (minExposureCompensation == 0 && maxExposureCompensation == 0) {
      callbackContext.error("Exposure corection not supported");
    } else {
      if (exposureCompensation < minExposureCompensation) {
        exposureCompensation = minExposureCompensation;
      } else if (exposureCompensation > maxExposureCompensation) {
        exposureCompensation = maxExposureCompensation;
      }
      params.setExposureCompensation(exposureCompensation);
      fragment.setCameraParameters(params);

      callbackContext.success(exposureCompensation);
    }
  }

  private void getExposureCompensationRange(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    int minExposureCompensation = camera.getParameters().getMinExposureCompensation();
    int maxExposureCompensation = camera.getParameters().getMaxExposureCompensation();

    if (minExposureCompensation == 0 && maxExposureCompensation == 0) {
      callbackContext.error("Exposure corection not supported");
    } else {
      JSONObject jsonExposureRange = new JSONObject();
      try {
        jsonExposureRange.put("min", new Integer(minExposureCompensation));
        jsonExposureRange.put("max", new Integer(maxExposureCompensation));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      callbackContext.success(jsonExposureRange);
    }
  }

  private void getSupportedWhiteBalanceModes(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    List<String> supportedWhiteBalanceModes;
    supportedWhiteBalanceModes = params.getSupportedWhiteBalance();

    JSONArray jsonWhiteBalanceModes = new JSONArray();
    if (camera.getParameters().isAutoWhiteBalanceLockSupported()) {
      jsonWhiteBalanceModes.put(new String("lock"));
    }
    if (supportedWhiteBalanceModes != null) {
      for (int i = 0; i < supportedWhiteBalanceModes.size(); i++) {
        jsonWhiteBalanceModes.put(new String(supportedWhiteBalanceModes.get(i)));
      }
    }
    callbackContext.success(jsonWhiteBalanceModes);
  }

  private void getWhiteBalanceMode(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    String whiteBalanceMode;

    if (camera.getParameters().isAutoWhiteBalanceLockSupported()) {
      if (camera.getParameters().getAutoWhiteBalanceLock()) {
        whiteBalanceMode = "lock";
      } else {
        whiteBalanceMode = camera.getParameters().getWhiteBalance();
      }
      ;
    } else {
      whiteBalanceMode = camera.getParameters().getWhiteBalance();
    }
    if (whiteBalanceMode != null) {
      callbackContext.success(whiteBalanceMode);
    } else {
      callbackContext.error("White balance mode not supported");
    }
  }

  private void setWhiteBalanceMode(String whiteBalanceMode, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    if (whiteBalanceMode.equals("lock")) {
      if (camera.getParameters().isAutoWhiteBalanceLockSupported()) {
        params.setAutoWhiteBalanceLock(true);
        fragment.setCameraParameters(params);
        callbackContext.success();
      } else {
        callbackContext.error("White balance lock not supported");
      }
    } else if (whiteBalanceMode.equals("auto") || whiteBalanceMode.equals("incandescent")
        || whiteBalanceMode.equals("cloudy-daylight") || whiteBalanceMode.equals("daylight")
        || whiteBalanceMode.equals("fluorescent") || whiteBalanceMode.equals("shade")
        || whiteBalanceMode.equals("twilight") || whiteBalanceMode.equals("warm-fluorescent")) {
      params.setWhiteBalance(whiteBalanceMode);
      fragment.setCameraParameters(params);
      callbackContext.success();
    } else {
      callbackContext.error("White balance parameter not supported");
    }
  }

  private void getMaxZoom(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    if (camera.getParameters().isZoomSupported()) {
      int maxZoom = camera.getParameters().getMaxZoom();
      callbackContext.success(maxZoom);
    } else {
      callbackContext.error("Zoom not supported");
    }
  }

  private void getHorizontalFOV(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    float horizontalViewAngle = params.getHorizontalViewAngle();

    callbackContext.success(String.valueOf(horizontalViewAngle));
  }

  private void getZoom(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    if (camera.getParameters().isZoomSupported()) {
      int getZoom = camera.getParameters().getZoom();
      callbackContext.success(getZoom);
    } else {
      callbackContext.error("Zoom not supported");
    }
  }

  private void setZoom(double zoom, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    if (params.isZoomSupported()) {
      List<Integer> zoomRatios = params.getZoomRatios();

      params.setZoom(calculateZoomIndex(zoom, zoomRatios));
      fragment.setCameraParameters(params);

      callbackContext.success();
    } else {
      callbackContext.error("Zoom not supported");
    }
  }

  private int calculateZoomIndex(double zoom, List<Integer> zoomRatios) {
    int zoomInBase100 = (int) (zoom * 100);
    int zoomIndex = 0;
    int size = zoomRatios.size();
    for (int i = 0; i < size; i++) {
      Integer zoomRatio = zoomRatios.get(i);
      if (zoomRatio < zoomInBase100) {
        zoomIndex = i;
      } else {
        break;
      }
    }

    return zoomIndex;
  }

  private void setScreenRotation(int screenRotation, CallbackContext callbackContext) {

    this.screenRotation = screenRotation;

    if (fragment != null && fragment.getCamera() != null) {
      setCameraRotation();
    }

    callbackContext.success();
  }

  private void setCameraRotation() {
    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    CameraInfo info = new CameraInfo();
    Camera.getCameraInfo(fragment.cameraCurrentlyLocked, info);

    int result;
    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { // front-facing
      result = (540 + screenRotation - info.orientation) % 360;
    } else { // back-facing
      result = (360 - screenRotation + info.orientation) % 360;
    }
    params.setRotation(result);
    fragment.setCameraParameters(params);
  }

  private void setPictureSize(int width, int height, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }
    Log.d(TAG, "setPictureSize: " + width + " " + height);

    fragment.setPictureSize(width, height);
    callbackContext.success();
  }

  private void getSupportedFlashModes(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();
    List<String> supportedFlashModes;
    supportedFlashModes = params.getSupportedFlashModes();
    JSONArray jsonFlashModes = new JSONArray();

    if (supportedFlashModes != null) {
      for (int i = 0; i < supportedFlashModes.size(); i++) {
        jsonFlashModes.put(new String(supportedFlashModes.get(i)));
      }
    }

    callbackContext.success(jsonFlashModes);
  }

  private void getSupportedFocusModes(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();
    List<String> supportedFocusModes;
    supportedFocusModes = params.getSupportedFocusModes();

    if (supportedFocusModes != null) {
      JSONArray jsonFocusModes = new JSONArray();
      for (int i = 0; i < supportedFocusModes.size(); i++) {
        jsonFocusModes.put(new String(supportedFocusModes.get(i)));
      }
      callbackContext.success(jsonFocusModes);
    } else {
      callbackContext.error("Camera focus modes parameters access error");
    }
  }

  private void getFocusMode(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    List<String> supportedFocusModes;
    supportedFocusModes = params.getSupportedFocusModes();

    if (supportedFocusModes != null) {
      String focusMode = params.getFocusMode();
      callbackContext.success(focusMode);
    } else {
      callbackContext.error("FocusMode not supported");
    }
  }

  private void setFocusMode(String focusMode, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    List<String> supportedFocusModes;
    List<String> supportedAutoFocusModes = Arrays.asList("auto", "continuous-picture", "continuous-video", "macro");
    supportedFocusModes = params.getSupportedFocusModes();
    if (supportedFocusModes.indexOf(focusMode) > -1) {
      params.setFocusMode(focusMode);
      fragment.setCameraParameters(params);
      callbackContext.success(focusMode);
    } else {
      callbackContext.error("Focus mode not recognised: " + focusMode);
    }
  }

  private void getFlashMode(CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    String flashMode = params.getFlashMode();

    if (flashMode != null) {
      callbackContext.success(flashMode);
    } else {
      callbackContext.error("FlashMode not supported");
    }
  }

  private void setFlashMode(String flashMode, CallbackContext callbackContext) {
    if (!this.hasCamera(callbackContext)) {
      return;
    }

    Camera camera = fragment.getCamera();
    Camera.Parameters params = camera.getParameters();

    List<String> supportedFlashModes;
    supportedFlashModes = camera.getParameters().getSupportedFlashModes();
    if (supportedFlashModes.indexOf(flashMode) > -1) {
      params.setFlashMode(flashMode);
    } else {
      callbackContext.error("Flash mode not recognised: " + flashMode);
      return;
    }

    fragment.setCameraParameters(params);
    callbackContext.success(flashMode);
  }

  private void stopCamera(CallbackContext callbackContext) {

    if (webViewParent != null) {
      cordova.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ((ViewGroup) webView.getView()).bringToFront();
          webViewParent = null;
        }
      });
    }

    if (!this.hasView(callbackContext)) {
      return;
    }

    FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.remove(fragment);
    fragmentTransaction.commit();
    fragment = null;

    // Hide the layout because it is not destroyed
    RelativeLayout containerView = cordova.getActivity().findViewById(containerViewId);
    containerView.setBackgroundColor(Color.TRANSPARENT);

    callbackContext.success();
  }

  private void showCamera(CallbackContext callbackContext) {
    if (!this.hasView(callbackContext)) {
      return;
    }

    FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.show(fragment);
    fragmentTransaction.commit();

    callbackContext.success();
  }

  private void hideCamera(CallbackContext callbackContext) {
    if (!this.hasView(callbackContext)) {
      return;
    }

    FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.hide(fragment);
    fragmentTransaction.commit();

    callbackContext.success();
  }

  private void tapToFocus(final int pointX, final int pointY, CallbackContext callbackContext) {
    if (!this.hasView(callbackContext)) {
      return;
    }

    setFocusCallbackContext = callbackContext;

    fragment.setFocusArea(pointX, pointY, new AutoFocusCallback() {
      public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
          camera.cancelAutoFocus();
          onFocusSet(pointX, pointY);
        } else {
          onFocusSetError("fragment.setFocusArea() failed");
        }
      }
    });
  }

  public void onFocusSet(final int pointX, final int pointY) {
    Log.d(TAG, "Focus set, returning coordinates");

    JSONObject data = new JSONObject();
    try {
      data.put("x", pointX);
      data.put("y", pointY);
    } catch (JSONException e) {
      Log.d(TAG, "onFocusSet failed to set output payload");
    }

    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
    pluginResult.setKeepCallback(true);
    setFocusCallbackContext.sendPluginResult(pluginResult);
  }

  public void onFocusSetError(String message) {
    Log.d(TAG, "CameraPreview onFocusSetError");
    setFocusCallbackContext.error(message);
  }

  private void switchCamera(CallbackContext callbackContext) {
    if (!this.hasView(callbackContext)) {
      return;
    }

    fragment.switchCamera();
    setCameraRotation();
    callbackContext.success();
  }

  public void setBackButtonListener(CallbackContext callbackContext) {
    tapBackButtonContext = callbackContext;
  }

  public void onBackButton() {
    if (tapBackButtonContext == null) {
      return;
    }
    Log.d(TAG, "Back button tapped, notifying");
    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "Back button pressed");
    tapBackButtonContext.sendPluginResult(pluginResult);
  }

}
