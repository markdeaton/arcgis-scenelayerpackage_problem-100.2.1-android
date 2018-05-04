/* Copyright 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.apl.slpkissue;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = "SceneLayer";
  private SceneView mSceneView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // create a scene and add a basemap to it
    final ArcGISScene scene = new ArcGISScene();
//    scene.setBasemap(Basemap.createImagery());

    mSceneView = (SceneView) findViewById(R.id.sceneView);
    mSceneView.setScene(scene);

    // add a scene service to the scene for viewing buildings

    // Find this at https://www.arcgis.com/home/item.html?id=9ea36cfa8152452b8cec2d663b1709e0
    String sFile = "Rancho_Mesh_mesh.slpk";
    // Find this at https://www.arcgis.com/home/item.html?id=3a68d6b2e0a047deacb6a46d39d1a766
//    String sFile = "cp_Pro_3Dobj_Global_Portland_symbo.slpk";
    File fLyrPkg = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        sFile);
    String sLyrPkg = fLyrPkg.getPath();
    String uriLyrPkg = Uri.fromFile(fLyrPkg).toString();

    final ArcGISSceneLayer sceneLayer = new ArcGISSceneLayer(sLyrPkg);
    sceneLayer.loadAsync();
    sceneLayer.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {
        if (sceneLayer.getLoadStatus() == LoadStatus.LOADED) {
          Log.d(TAG, "Layer loaded");
          scene.getOperationalLayers().add(sceneLayer);
          Envelope env = sceneLayer.getFullExtent();
          Point ptCenter = (Point) GeometryEngine.project(
              env.getCenter(), mSceneView.getSpatialReference());
          // add a camera and initial camera position
          Camera camera = new Camera(ptCenter.getX(), ptCenter.getY(), 350,0, 0, 0);
//          Camera camera = new Camera(37.7605, -119.5615, 4800, 270, 65, 0);
//          Camera camera = new Camera(48.378, -4.494, 200, 345, 65, 0);
          mSceneView.setViewpointCamera(camera);
        } else {
          ArcGISRuntimeException exc = sceneLayer.getLoadError();
          String sErr = exc.getLocalizedMessage();
          sErr += exc.getLocalizedMessage().length() > 0 ? "; " + exc.getLocalizedMessage() : "";
          if (exc.getCause() != null) sErr += exc.getCause().getLocalizedMessage();
          Log.e(TAG, "Layer failed to load: " + sErr, exc);
        }
      }
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
    // pause SceneView
    mSceneView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // resume SceneView
    mSceneView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // dispose SceneView
    mSceneView.dispose();
  }
}
