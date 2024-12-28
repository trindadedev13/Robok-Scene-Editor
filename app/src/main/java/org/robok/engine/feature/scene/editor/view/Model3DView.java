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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import java.lang.reflect.Method;
import java.util.ArrayList;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import org.robok.engine.feature.scene.editor.controller.CameraInputController2;
import org.robok.engine.feature.scene.editor.objects.ObjectsCreator;
import org.robok.engine.feature.scene.editor.objects.SceneObject;

public class Model3DView extends ApplicationAdapter {

  public static Model3DView instance;

  private PerspectiveCamera camera;
  private ModelBatch modelBatch;
  private ModelInstance modelInstance;
  private CameraInputController2 camController;
  private FirstPersonCameraController personController;
  // bordas
  private ShapeRenderer shapeRenderer;
  ModelInstance planeInstance;

  // Lista de todos os objetos

  private SpriteBatch spriteBatch;
  private BitmapFont font;
  private GlyphLayout layout;
  public String objectCommand = null;

  private Cubemap diffuseCubemap;
  private Cubemap environmentCubemap;
  private Cubemap specularCubemap;
  private SceneManager sceneManager;
  private SceneSkybox skybox;
  private Texture brdfLUT;

  public Model3DView() {
    instance = this;
    SceneEditorView.instance.sceneState.objects = new ArrayList<>();
  }

  @Override
  public void create() {
    // Inicializa o batch e a fonte
    spriteBatch = new SpriteBatch();
    font = new BitmapFont(); // Cria uma fonte padrão
    layout = new GlyphLayout();

    // Configura a câmera
    camera = new PerspectiveCamera(80, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.position.set(0f, 10f, 0f);
    camera.lookAt(0f, 0f, 0f);
    camera.near = 1f;
    camera.far = 300f;
    camera.update();

    // Cria o batch para desenhar o modelo
    modelBatch = new ModelBatch();

    // Configura o controle da câmera
    camController = new CameraInputController2(camera);
    Gdx.input.setInputProcessor(camController);

    initialCube();
    shapeRenderer = new ShapeRenderer();

    sceneManager = new SceneManager();
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
  }

  @Override
  public void render() {
    // Configurações iniciais
    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    camController.update();
    camera.update();

    // Renderizar o Skybox
    Gdx.gl.glDepthMask(false); // Desabilitar a escrita no Depth Buffer
    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    Gdx.gl.glCullFace(GL20.GL_FRONT); // Renderizar o interior do cubo

    modelBatch.begin(camera);

    float deltaTime = Gdx.graphics.getDeltaTime();

    sceneManager.update(deltaTime);
    sceneManager.render();

    shapeRenderer.setProjectionMatrix(camera.combined);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    drawGrid3D(0, 0, 0, 200, 200, 1, 0.1f); // Função para desenhar grade
    shapeRenderer.end();

    onTime();
    for (SceneObject scene : SceneEditorView.instance.sceneState.objects) {
      modelBatch.render(scene.getModelInstance());
    }

    modelBatch.end();

    // Restaurar configurações de depth e culling
    // Gdx.gl.glDepthMask(true);
    Gdx.gl.glCullFace(GL20.GL_BACK);

    // Renderizando outros objetos

    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

    // Renderizando texto
    spriteBatch.begin();
    String text = "Skybox funcionando!";
    layout.setText(font, text);
    font.draw(spriteBatch, text, 10, Gdx.graphics.getHeight() - 10);
    spriteBatch.end();
  }

  private void initialCube() {

    Vector3 size = new Vector3(4f, 4f, 4f);

    ModelBuilder modelBuilder = new ModelBuilder();
    modelBuilder.begin();
    modelBuilder.node().id = "cube";
    modelBuilder
        .part(
            "cube",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.GREEN)))
        .box(4f, 4f, 4f);
    Model modelB = modelBuilder.end();
    modelInstance = new ModelInstance(modelB);
    modelInstance.transform.setToTranslation(0f, 0f, 0f);

    SceneEditorView.instance.sceneState.objects.add(new SceneObject(modelInstance.model, size, modelInstance));
  }

  public static Vector3 getModelDimensions(Model model) {
    Vector3 min = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    Vector3 max = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

    for (MeshPart meshPart : model.meshParts) {
      int numVertices = meshPart.mesh.getNumVertices();
      float[] vertices = new float[numVertices];
      meshPart.mesh.getVertices(vertices);

      for (int i = 0; i < numVertices; i += 3) {
        float x = vertices[i];
        float y = vertices[i + 1];
        float z = vertices[i + 2];

        if (x < min.x) min.x = x;
        if (y < min.y) min.y = y;
        if (z < min.z) min.z = z;

        if (x > max.x) max.x = x;
        if (y > max.y) max.y = y;
        if (z > max.z) max.z = z;
      }
    }

    Vector3 dimensions = new Vector3();
    dimensions.set(max).sub(min);
    return dimensions;
  }

  public void renderEdges(ShapeRenderer shapeRenderer, ModelInstance modelInstance) {

    if (modelInstance != null) {
      Model model = modelInstance.model;

      //  shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

      for (int i = 0; i < model.meshes.size; i++) {
        Mesh mesh = model.meshes.get(i);
        float[] vertices = new float[mesh.getMaxVertices()];
        short[] indices = new short[mesh.getMaxIndices()];
        mesh.getVertices(vertices);
        mesh.getIndices(indices);

        for (int j = 0; j < indices.length; j += 2) {
          Vector3 v1 =
              new Vector3(
                  vertices[indices[j] * 3],
                  vertices[indices[j] * 3 + 1],
                  vertices[indices[j] * 3 + 2]);
          Vector3 v2 =
              new Vector3(
                  vertices[indices[j + 1] * 3],
                  vertices[indices[j + 1] * 3 + 1],
                  vertices[indices[j + 1] * 3 + 2]);

          shapeRenderer.line(v1, v2);
        }
      }

      //  shapeRenderer.end();
    }
  }

  @Override
  public void dispose() {
    modelBatch.dispose();
    for (SceneObject scene : SceneEditorView.instance.sceneState.objects) {
      scene.getModelInstance().model.dispose();
    }
    sceneManager.dispose();
    skybox.dispose();
    environmentCubemap.dispose();
    diffuseCubemap.dispose();
    specularCubemap.dispose();
    brdfLUT.dispose();
    //	skybox.dispose();
  }

  public void setCommand(String objectCommand) {
    this.objectCommand = objectCommand;
  }

  private void invokeObject(String objectCommand) {
    try {
      ObjectsCreator createObjects = new ObjectsCreator(camController, SceneEditorView.instance.sceneState.objects);
      Class<?> instance = createObjects.getClass();
      Method method = instance.getDeclaredMethod(objectCommand);
      method.invoke(createObjects);
      // sceneObjects = createObjects.get();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void onTime() {
    if (objectCommand != null) {
      invokeObject(objectCommand);
      objectCommand = null;
    }
  }

  // Desenhar cubo simples
  private void drawCubeEdges(float size) {
    float halfSize = size / 2;

    // Define os pontos do cubo
    float[][] points = {
      {-halfSize, -halfSize, -halfSize},
      {halfSize, -halfSize, -halfSize},
      {halfSize, halfSize, -halfSize},
      {-halfSize, halfSize, -halfSize},
      {-halfSize, -halfSize, halfSize},
      {halfSize, -halfSize, halfSize},
      {halfSize, halfSize, halfSize},
      {-halfSize, halfSize, halfSize}
    };

    // Desenha as arestas do cubo
    for (int i = 0; i < 4; i++) {
      shapeRenderer.line(
          points[i][0],
          points[i][1],
          points[i][2],
          points[(i + 1) % 4][0],
          points[(i + 1) % 4][1],
          points[(i + 1) % 4][2]);
      shapeRenderer.line(
          points[i + 4][0],
          points[i + 4][1],
          points[i + 4][2],
          points[((i + 1) % 4) + 4][0],
          points[((i + 1) % 4) + 4][1],
          points[((i + 1) % 4) + 4][2]);
      shapeRenderer.line(
          points[i][0],
          points[i][1],
          points[i][2],
          points[i + 4][0],
          points[i + 4][1],
          points[i + 4][2]);
    }
  }

  // Desenhar cubo detalhado
  private void drawCubeEdges2(
      float scaleX, float scaleY, float scaleZ, float centerX, float centerY, float centerZ) {
    // Calcula os tamanhos no cubo
    float halfScaleX = scaleX / 2;
    float halfScaleY = scaleY / 2;
    float halfScaleZ = scaleZ / 2;

    // Define os pontos do cubo com escalas e posição central
    float[][] points = {
      {centerX - halfScaleX, centerY - halfScaleY, centerZ - halfScaleZ},
      {centerX + halfScaleX, centerY - halfScaleY, centerZ - halfScaleZ},
      {centerX + halfScaleX, centerY + halfScaleY, centerZ - halfScaleZ},
      {centerX - halfScaleX, centerY + halfScaleY, centerZ - halfScaleZ},
      {centerX - halfScaleX, centerY - halfScaleY, centerZ + halfScaleZ},
      {centerX + halfScaleX, centerY - halfScaleY, centerZ + halfScaleZ},
      {centerX + halfScaleX, centerY + halfScaleY, centerZ + halfScaleZ},
      {centerX - halfScaleX, centerY + halfScaleY, centerZ + halfScaleZ}
    };

    shapeRenderer.setColor(Color.WHITE);
    // Desenha as arestas do cubo
    for (int i = 0; i < 4; i++) {
      shapeRenderer.line(
          points[i][0],
          points[i][1],
          points[i][2],
          points[(i + 1) % 4][0],
          points[(i + 1) % 4][1],
          points[(i + 1) % 4][2]);
      shapeRenderer.line(
          points[i + 4][0],
          points[i + 4][1],
          points[i + 4][2],
          points[((i + 1) % 4) + 4][0],
          points[((i + 1) % 4) + 4][1],
          points[((i + 1) % 4) + 4][2]);
      shapeRenderer.line(
          points[i][0],
          points[i][1],
          points[i][2],
          points[i + 4][0],
          points[i + 4][1],
          points[i + 4][2]);
    }
  }

  // Grades planos com linhas no centro
  private void drawGrid3D(
      float centerX,
      float centerY,
      float centerZ,
      float width,
      float depth,
      float cellSize,
      float lineThickness) {
    // Calcular os limites do plano
    float startX = centerX - width / 2;
    float endX = centerX + width / 2;
    float startZ = centerZ - depth / 2;
    float endZ = centerZ + depth / 2;
    shapeRenderer.setColor(Color.GRAY);

    // Desenha linhas horizontais (ao longo do eixo X)
    for (float z = startZ; z <= endZ; z += cellSize) {
      if (Math.abs(z - centerZ) < cellSize / 2) {
        // define cor
        //  shapeRenderer.setColor(Color.GRAY);
        // Desenha a linha central mais grossa com múltiplas linhas próximas
        shapeRenderer.line(
            startX, centerY, z - lineThickness / 2, endX, centerY, z - lineThickness / 2);
        shapeRenderer.line(
            startX, centerY, z + lineThickness / 2, endX, centerY, z + lineThickness / 2);
      } else {
        shapeRenderer.line(startX, centerY, z, endX, centerY, z);
      }
    }

    // Desenha linhas verticais (ao longo do eixo Z)
    for (float x = startX; x <= endX; x += cellSize) {
      if (Math.abs(x - centerX) < cellSize / 2) {
        // define cor
        //   shapeRenderer.setColor(Color.GRAY);
        // Desenha a linha central mais grossa com múltiplas linhas próximas
        shapeRenderer.line(
            x - lineThickness / 2, centerY, startZ, x - lineThickness / 2, centerY, endZ);
        shapeRenderer.line(
            x + lineThickness / 2, centerY, startZ, x + lineThickness / 2, centerY, endZ);
      } else {
        shapeRenderer.line(x, centerY, startZ, x, centerY, endZ);
      }
    }
  }

  // Grades planos sem linhas no centro
  private void drawGrid3D(
      float centerX, float centerY, float centerZ, float width, float depth, float cellSize) {
    // Calcular os limites do plano
    float startX = centerX - width / 2;
    float endX = centerX + width / 2;
    float startZ = centerZ - depth / 2;
    float endZ = centerZ + depth / 2;

    // Desenha linhas horizontais (ao longo do eixo X)
    for (float z = startZ; z <= endZ; z += cellSize) {
      shapeRenderer.line(startX, centerY, z, endX, centerY, z);
    }

    // Desenha linhas verticais (ao longo do eixo Z)
    for (float x = startX; x <= endX; x += cellSize) {
      shapeRenderer.line(x, centerY, startZ, x, centerY, endZ);
    }
  }

  // desenha de forma vertical
  private void drawGridParede(
      float startX, float startY, float width, float height, float cellSize) {
    // Desenha linhas horizontais
    for (float y = startY; y <= startY + height; y += cellSize) {
      shapeRenderer.line(startX, y, startX + width, y);
    }

    // Desenha linhas verticais
    for (float x = startX; x <= startX + width; x += cellSize) {
      shapeRenderer.line(x, startY, x, startY + height);
    }
  }
}
