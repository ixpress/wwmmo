package au.com.codeka.warworlds.game.starfield;

import android.opengl.GLES20;

import org.andengine.entity.Entity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.shader.ShaderProgram;
import org.andengine.opengl.shader.constants.ShaderProgramConstants;
import org.andengine.opengl.shader.exception.ShaderProgramLinkException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes;

/**
 * This entity is used to display the tactical "overlay" that is used to color the space around
 * stars owned by an empire.
 *
 * It takes the texture generated by {@link TacticalBitmapTextureSource} and applies a bit of a blur
 * to it.
 */
public class TacticalOverlayEntity extends Entity {
  private static final MyShaderProgram SHADER_PROGRAM = new MyShaderProgram();

  private Sprite sprite;

  public TacticalOverlayEntity(float x, float y, float width, float height,
      ITextureRegion textureRegion, VertexBufferObjectManager vertexBufferObjectManager) {
    super(0, 0, 1, 1);

    sprite = new Sprite(x, y, width, height, textureRegion, vertexBufferObjectManager);
    sprite.setShaderProgram(SHADER_PROGRAM);
    attachChild(sprite);
  }

  private static class MyShaderProgram extends ShaderProgram {
    public static int sUniformModelViewPositionMatrixLocation = ShaderProgramConstants.LOCATION_INVALID;
    public static int sTextureLocation = ShaderProgramConstants.LOCATION_INVALID;

    public static final String VERTEX_SHADER =
        "uniform mat4 " + ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX + ";\n"
        + "attribute vec4 " + ShaderProgramConstants.ATTRIBUTE_POSITION + ";\n"
        + "attribute vec2 " + ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES + ";\n"
        + "varying vec2 " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n"
        + "void main() {\n"
        + "   " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + " = " + ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES + ";\n"
        + "   gl_Position = " + ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX + " * " + ShaderProgramConstants.ATTRIBUTE_POSITION + ";\n"
        + "}";

    public static final String FRAGMENT_SHADER = "precision mediump float;\n"
        + "uniform sampler2D " + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ";\n"
        + "varying vec2 " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n"
        + "void main() {\n"
        + "   gl_FragColor = texture2D(" + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ", " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ");\n"
        + "}";

    public MyShaderProgram() {
      super(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    protected void link(final GLState pGLState) throws ShaderProgramLinkException {
      GLES20.glBindAttribLocation(mProgramID,
          ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION,
          ShaderProgramConstants.ATTRIBUTE_POSITION);
      GLES20.glBindAttribLocation(mProgramID,
          ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES_LOCATION,
          ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES);

      super.link(pGLState);

      sUniformModelViewPositionMatrixLocation = getUniformLocation(
          ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX);
      sTextureLocation = getUniformLocation(ShaderProgramConstants.UNIFORM_TEXTURE_0);
    }

    @Override
    public void bind(final GLState pGLState,
        final VertexBufferObjectAttributes pVertexBufferObjectAttributes) {
      GLES20.glDisableVertexAttribArray(ShaderProgramConstants.ATTRIBUTE_COLOR_LOCATION);
      super.bind(pGLState, pVertexBufferObjectAttributes);
      GLES20.glUniformMatrix4fv(sUniformModelViewPositionMatrixLocation, 1, false,
          pGLState.getModelViewProjectionGLMatrix(), 0);
      GLES20.glBindTexture(sTextureLocation, pGLState.getActiveTexture());
    }

    @Override
    public void unbind(final GLState pGLState) {
      GLES20.glEnableVertexAttribArray(ShaderProgramConstants.ATTRIBUTE_COLOR_LOCATION);
      super.unbind(pGLState);
    }
  }
}