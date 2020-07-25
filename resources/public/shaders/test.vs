attribute vec4 position;

void main() {
  gl_Position = position;
}

// (webgl.core/load-shaders "test.vs" "test.fs")
