package com.acatapps.videomaker.modules.encode

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class StickerRender(val isExternalEOS: Boolean = true)  {

        private val vidVertexShaderCode =
            """
        precision highp float;
        attribute vec3 vertexPosition;
        attribute vec2 uvs;
        varying vec2 varUvs;
        uniform mat4 texMatrix;
      
        void main()
        {
            varUvs = (texMatrix * vec4(uvs.x, uvs.y, 0, 1.0)).xy;
            gl_Position = mvp * vec4(vertexPosition, 1.0);
        }
        """

        private val vidFragmentShaderCode =
            """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        
        varying vec2 varUvs;
        uniform samplerExternalOES texSampler;
        
        void main()
        {
            gl_FragColor = texture2D(texSampler, varUvs);
        }
        """

        private val textVertexShaderCode =
            """                       
        precision highp float;
        attribute vec3 vertexPosition;
        attribute vec2 uvs;
        varying vec2 varUvs;
        uniform mat4 mvp;
        
        void main()
        {
            varUvs = uvs;
            gl_Position = mvp * vec4(vertexPosition, 1.0);
        }
        """

        private val textFragmentShaderCode =
            """
        precision mediump float;         
        varying vec2 varUvs;
        uniform sampler2D texSampler;
        
        void main()
        {
            gl_FragColor = texture2D(texSampler, varUvs);
        }
        """

        private var vertices = floatArrayOf(

            -1.0f, -1.0f, 0.0f, 0f, 0f,
            -1.0f, 1.0f, 0.0f, 0f, 1f,
            1.0f, 1.0f, 0.0f, 1f, 1f,
            1.0f, -1.0f, 0.0f, 1f, 0f
        )

        private var indices = intArrayOf(
            2, 1, 0, 0, 3, 2
        )

        private var program: Int
        private var vertexHandle: Int = 0
        private var bufferHandles = IntArray(2)
        private var uvsHandle: Int = 0
        private var texMatrixHandle: Int = 0
        private var mvpHandle: Int = 0
        private var samplerHandle: Int = 0
        private val textureHandles = IntArray(1)

        val texId: Int
            get() = textureHandles[0]

        private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        private var indexBuffer: IntBuffer = ByteBuffer.allocateDirect(indices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asIntBuffer().apply {
                put(indices)
                position(0)
            }
        }

        init {
            val vtxCode = if (isExternalEOS) vidVertexShaderCode else textVertexShaderCode
            val fragCode = if (isExternalEOS) vidFragmentShaderCode else textFragmentShaderCode
            val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vtxCode)
            val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragCode)

            program = GLES20.glCreateProgram().also {

                GLES20.glAttachShader(it, vertexShader)
                GLES20.glAttachShader(it, fragmentShader)
                GLES20.glLinkProgram(it)

                vertexHandle = GLES20.glGetAttribLocation(it, "vertexPosition")
                uvsHandle = GLES20.glGetAttribLocation(it, "uvs")
                if (isExternalEOS) texMatrixHandle = GLES20.glGetUniformLocation(it, "texMatrix")
                samplerHandle = GLES20.glGetUniformLocation(it, "texSampler")
            }

            GLES20.glGenBuffers(2, bufferHandles, 0)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES20.GL_DYNAMIC_DRAW)

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indexBuffer, GLES20.GL_DYNAMIC_DRAW)


            val target = if (isExternalEOS) GLES11Ext.GL_TEXTURE_EXTERNAL_OES else GLES20.GL_TEXTURE_2D
            GLES20.glGenTextures(1, textureHandles, 0)
            GLES20.glBindTexture(target, textureHandles[0])
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST)
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR)
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE)

            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        }

        private fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }

        fun draw(mvpMatrix: FloatArray,
                 texMatrix:FloatArray?,
                 bitmap: Bitmap?) {

            GLES20.glUseProgram(program)

            texMatrix?.let {
                GLES20.glUniformMatrix4fv(texMatrixHandle, 1, false, it, 0)
            }


            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

            val target = if (isExternalEOS) GLES11Ext.GL_TEXTURE_EXTERNAL_OES else GLES20.GL_TEXTURE_2D
            GLES20.glBindTexture(target, textureHandles[0])

            bitmap?.let {
                GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1)
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, it, 0)
            }

            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST)
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR)
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])

            GLES20.glEnableVertexAttribArray(vertexHandle)
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 4 * 5, 0)

            GLES20.glEnableVertexAttribArray(uvsHandle)
            GLES20.glVertexAttribPointer(uvsHandle, 2, GLES20.GL_FLOAT, false, 4 * 5, 3 * 4)

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0)
        }

}