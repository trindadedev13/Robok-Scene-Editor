package org.robok.engine.feature.scene.editor.objects;

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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import java.util.List;
import org.robok.engine.feature.scene.editor.view.SceneEditorView;
import org.robok.engine.feature.scene.editor.controller.CameraInputController2;

public class ObjectsCreator {
  private final List<SceneObject> sceneObjects;
  private final CameraInputController2 controller;
  private final ModelBuilder modelBuilder;
  private final Vector3 size;

  public ObjectsCreator(
      final CameraInputController2 controller, final List<SceneObject> sceneObjects) {
    this.controller = controller;
    this.sceneObjects = sceneObjects;
    this.modelBuilder = new ModelBuilder();
    this.size = new Vector3(4f, 4f, 4f);
  }

  public List<SceneObject> get() {
    return this.sceneObjects;
  }

  public void createCube() {
    size.set(4f, 4f, 4f);
    modelBuilder.begin();
    modelBuilder.node().id = "cubee";
    modelBuilder
        .part(
            "cubee",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.CYAN)))
        .box(size.x, size.y, size.z);
    Model model = modelBuilder.end();
    ModelInstance instance = new ModelInstance(model);
    instance.transform.setToTranslation(controller.target.cpy());
    SceneEditorView.instance.sceneState.objects.add(new SceneObject(model, size, instance));
  }

  public void createTriangle() {
    size.set(2f, 6f, 4f);
    modelBuilder.begin();
    modelBuilder.node().id = "capsule";
    modelBuilder
        .part(
            "capsule",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.BLUE)))
        .capsule(size.x, size.y, 20);
    Model model = modelBuilder.end();
    ModelInstance instance = new ModelInstance(model);
    instance.transform.setToTranslation(controller.target.cpy());
    this.sceneObjects.add(new SceneObject(model, size, instance));
  }

  public void createTriangle2D() {
    modelBuilder.begin();
    MeshPartBuilder meshPartBuilder =
        modelBuilder.part(
            "triangle",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.GREEN)));

    // Define os vértices do triângulo
    Vector3 v1 = new Vector3(-1f, 0f, 0f);
    Vector3 v2 = new Vector3(1f, 0f, 0f);
    Vector3 v3 = new Vector3(0f, 1f, 0f);

    // Normal do triângulo (perpendicular à face)
    Vector3 normal = new Vector3(0f, 0f, 1f);

    // Adiciona os vértices e a normal ao MeshPartBuilder
    meshPartBuilder.vertex(v1, normal, null, null);
    meshPartBuilder.vertex(v2, normal, null, null);
    meshPartBuilder.vertex(v3, normal, null, null);

    // Define os índices que conectam os vértices para formar o triângulo
    meshPartBuilder.index((short) 0, (short) 1, (short) 2);

    Model model = modelBuilder.end();
    ModelInstance instance = new ModelInstance(model);
    instance.transform.setToTranslation(controller.target.cpy());
    this.sceneObjects.add(new SceneObject(model, size, instance));
  }

  // Método para adicionar uma esfera ao ModelBuilder
  public void createSphere() {
    size.set(4f, 4f, 4f);
    modelBuilder.begin();
    modelBuilder.node().id = "sphere";
    modelBuilder
        .part(
            "sphere",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.RED)))
        .sphere(size.x, size.y, size.z, 20, 20);
    Model model = modelBuilder.end();
    ModelInstance instance = new ModelInstance(model);
    instance.transform.setToTranslation(controller.target.cpy());
    this.sceneObjects.add(new SceneObject(model, size, instance));
  }

  public void createCylinder() {
    size.set(2f, 6f, 2f);
    modelBuilder.begin();
    modelBuilder.node().id = "cylinder";
    modelBuilder
        .part(
            "cylinder",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.BLUE)))
        .cylinder(size.x, size.y, size.z, 20);
    Model model = modelBuilder.end();
    ModelInstance instance = new ModelInstance(model);
    instance.transform.setToTranslation(controller.target.cpy());
    this.sceneObjects.add(new SceneObject(model, size, instance));
  }

  public void createCone() {
    size.set(4f, 6f, 4f);
    modelBuilder.begin();
    modelBuilder.node().id = "cone";
    modelBuilder
        .part(
            "cone",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
        .cone(size.x, size.y, size.z, 20);
    Model model = modelBuilder.end();
    ModelInstance instance = new ModelInstance(model);
    instance.transform.setToTranslation(controller.target.cpy());
    this.sceneObjects.add(new SceneObject(model, size, instance));
  }

  public void createPlane() {
    modelBuilder.begin();
    modelBuilder.node().id = "plane";
    modelBuilder
        .part(
            "plane",
            GL20.GL_TRIANGLES,
            new VertexAttributes(
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")),
            new Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
        .rect(
            new Vector3(-5f, 0f, -5f),
            new Vector3(5f, 0f, -5f),
            new Vector3(5f, 0f, 5f),
            new Vector3(-5f, 0f, 5f),
            new Vector3(0f, 1f, 0f));
    Model model = modelBuilder.end();
    ModelInstance instance = new ModelInstance(model);
    instance.transform.setToTranslation(controller.target.cpy());
    this.sceneObjects.add(new SceneObject(model, size, instance));
  }

  public Model createFinalModel() {
    createCube();
    createTriangle();
    createTriangle2D();
    createSphere();
    createCylinder();
    createCone();
    createPlane();

    return modelBuilder.end();
  }
}
