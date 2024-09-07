package com.github.jacksonhoggard.voodoo2d.engine.graphic;

import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {

    private final int vaoId;

    private final List<Integer> vboIdList;

    private final int vertexCount;

    private final SpriteSheet spriteSheet;

    private Texture texture;

    private final boolean hasSpriteSheet;

    private int currentFrame;

    public Mesh(float[] positions, float[] textCoords, int[] indices, Texture texture) {
        this(positions, textCoords, indices, (SpriteSheet) null);
        this.texture = texture;
    }

    public Mesh(float[] positions, float[] textCoords, int[] indices, SpriteSheet spriteSheet) {
        FloatBuffer posBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            hasSpriteSheet = spriteSheet != null;
            this.spriteSheet = spriteSheet;
            currentFrame = 0;
            vertexCount = indices.length;
            vboIdList = new ArrayList<>();

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Texture coordinates VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Index VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (textCoordsBuffer != null) {
                MemoryUtil.memFree(textCoordsBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public static Mesh loadMesh(String filename, int size) {
        return new Mesh(
                new float[]{ // positions
                    -0.5f,  0.5f, 0.0f,
                    -0.5f, -0.5f, 0.0f,
                    0.5f, -0.5f, 0.0f,
                    0.5f,  0.5f, 0.0f,
                },
                new float[]{ // text coordinates
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                },
                new int[]{
                    0, 1, 3, 3, 1, 2,
                },
                new SpriteSheet(filename, size)
        );
    }

    public static Mesh loadMesh(String filename) throws Exception {
        BufferedImage tmp = ImageIO.read(new File(filename));
        Vector2f topRight, bottomRight, bottomLeft;
        if(tmp.getWidth() > tmp.getHeight()) {
            topRight = new Vector2f(0.25f + (1f / tmp.getWidth()), 0.5f);
            bottomLeft = new Vector2f(-0.5f, -((1f / tmp.getHeight()) * 2));
            bottomRight = new Vector2f(topRight.x, bottomLeft.y);
        } else {
            topRight = new Vector2f((1f / tmp.getWidth()), 0.5f);
            bottomLeft = new Vector2f(-0.5f, -0.5f);
            bottomRight = new Vector2f(topRight.x, -0.5f);
        }
        if(tmp.getWidth() == tmp.getHeight()) {
            topRight = new Vector2f(0.5f, 0.5f);
            bottomLeft = new Vector2f(-0.5f, -0.5f);
            bottomRight = new Vector2f(0.5f, -0.5f);
        }
        return new Mesh(
                new float[]{ // positions
                    -0.5f,  0.5f, 0.0f,
                    bottomLeft.x, bottomLeft.y, 0.0f,
                    bottomRight.x, bottomRight.y, 0.0f,
                    topRight.x,  topRight.y, 0.0f,
                },
                new float[]{ // text coordinates
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                },
                new int[]{
                    0, 1, 3, 3, 1, 2,
                },
                new Texture(filename)
        );
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the texture
        if(hasSpriteSheet) {
            for(Texture t : spriteSheet.getTextures()) {
                t.cleanup();
            }
        } else {
            texture.cleanup();
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public SpriteSheet getSpriteSheet() {
        return spriteSheet;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public int getCurrentTextureId(){
        return hasSpriteSheet ? spriteSheet.getTextures()[currentFrame].getId() : texture.getId();
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }
}