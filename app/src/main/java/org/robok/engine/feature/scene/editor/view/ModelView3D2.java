package org.robok.engine.feature.scene.editor.view;

/*
 *  This file is part of Robok © 2024.
 *
 *  Robok is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Robok is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Robok.  If not, see <https://www.gnu.org/licenses/>.
 */

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import org.robok.engine.feature.scene.editor.controller.CameraInputController2;

public class ModelView3D2 extends ApplicationAdapter {
  private SceneManager sceneManager;
  private SceneAsset sceneAsset;
  private Scene scene;
  private PerspectiveCamera camera;
  private Cubemap diffuseCubemap;
  private Cubemap environmentCubemap;
  private Cubemap specularCubemap;
  private Texture brdfLUT;
  private float time;
  private SceneSkybox skybox;

  private CameraInputController2 camController;

  @Override
  public void create() {
    // create scene
    //	sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/BoomBox/glTF/BoomBox.gltf"));
    //	scene = new Scene(sceneAsset.scene);
    sceneManager = new SceneManager();
    //	sceneManager.addScene(scene);

    // setup camera
    camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    float d = .02f;
    camera.near = d / 1000f;
    camera.far = 1000f;
    sceneManager.setCamera(camera);

    // setup IBL (image based lighting)
    environmentCubemap =
        EnvironmentUtil.createCubemap(
            new InternalFileHandleResolver(),
            "skyscene/sky_",
            ".png",
            EnvironmentUtil.FACE_NAMES_NEG_POS);
    diffuseCubemap =
        EnvironmentUtil.createCubemap(
            new InternalFileHandleResolver(),
            "textures/demo1/diffuse/diffuse_",
            ".jpg",
            EnvironmentUtil.FACE_NAMES_NEG_POS);
    specularCubemap =
        EnvironmentUtil.createCubemap(
            new InternalFileHandleResolver(),
            "textures/demo1/specular/specular_",
            "_",
            ".jpg",
            10,
            EnvironmentUtil.FACE_NAMES_NEG_POS);
    brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

    sceneManager.setAmbientLight(1f);
    sceneManager.environment.set(
        new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
    sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
    sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

    // setup skybox
    skybox = new SceneSkybox(environmentCubemap);
    sceneManager.setSkyBox(skybox);

    camController = new CameraInputController2(camera);
    Gdx.input.setInputProcessor(camController);
  }

  @Override
  public void resize(int width, int height) {
    sceneManager.updateViewport(width, height);
  }

  @Override
  public void render() {

    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    float deltaTime = Gdx.graphics.getDeltaTime();
    camController.update();
    camera.update();

    sceneManager.update(deltaTime);
    sceneManager.render();
  }

  @Override
  public void dispose() {
    sceneManager.dispose();
    sceneAsset.dispose();
    environmentCubemap.dispose();
    diffuseCubemap.dispose();
    specularCubemap.dispose();
    brdfLUT.dispose();
    skybox.dispose();
  }
}
