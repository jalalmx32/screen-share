#!/bin/bash
# ScreenShare APK Builder
# Uses persistent keystore - no more conflicts!

export ANDROID_HOME=/opt/android-sdk
export BUILD_TOOLS=$ANDROID_HOME/build-tools/34.0.0
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$BUILD_TOOLS:$PATH

# Paths
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/android2/app/src/main"
KEYSTORE="$PROJECT_DIR/android-release.jks"
BUILD_DIR="/tmp/build_apk"
OUTPUT_APK="$PROJECT_DIR/android2/ScreenShare.apk"

# Version from args
VERSION=${1:-"3.2.0"}
OUTPUT_DIR="$PROJECT_DIR/android2/ScreenShare-v${VERSION}.apk"

echo "=== Building ScreenShare v${VERSION} ==="

# Clean
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"/{gen,classes,dex,compiled_res,apk_contents}

# Step 1: Compile resources
echo "[1/6] Compile resources..."
$BUILD_TOOLS/aapt2 compile --dir "$SRC_DIR/res" -o "$BUILD_DIR/compiled_res/" 2>&1

# Step 2: Link resources (include assets directory)
echo "[2/6] Link resources..."
$BUILD_TOOLS/aapt2 link \
  --manifest "$SRC_DIR/AndroidManifest.xml" \
  -I "$ANDROID_HOME/platforms/android-34/android.jar" \
  --auto-add-overlay \
  --java "$BUILD_DIR/gen" \
  -A "$SRC_DIR/assets" \
  -o "$BUILD_DIR/app.unsigned.apk" \
  "$BUILD_DIR/compiled_res/"*.flat 2>&1

# Step 3: Compile Java
echo "[3/6] Compile Java..."
# Find all Java source files
find "$SRC_DIR/java" -name "*.java" > "$BUILD_DIR/java_sources.txt"
java -jar /tmp/ecj.jar -source 1.8 -target 1.8 \
  -classpath "$ANDROID_HOME/platforms/android-34/android.jar" \
  -d "$BUILD_DIR/classes" \
  -sourcepath "$SRC_DIR/java:$BUILD_DIR/gen" \
  "$BUILD_DIR/gen/com/screenshare/app/R.java" \
  "$SRC_DIR/java/com/screenshare/app/MainActivity.java" 2>&1 | grep -E "error|ERROR" || echo "  Java compilation OK"

# Step 4: Create DEX
echo "[4/6] Create DEX..."
cd "$BUILD_DIR/classes" && jar cf "$BUILD_DIR/all-classes.jar" . && cd "$BUILD_DIR"
d8 --output "$BUILD_DIR/dex" \
  --lib "$ANDROID_HOME/platforms/android-34/android.jar" \
  "$BUILD_DIR/all-classes.jar" 2>&1

# Step 5: Package with assets and resources
echo "[5/6] Package & Sign..."
cd "$BUILD_DIR"
unzip -q -o app.unsigned.apk -d apk_contents/
cp dex/classes.dex apk_contents/

# Ensure assets are in the APK
if [ -d "$SRC_DIR/assets" ]; then
    echo "  Copying assets to APK..."
    mkdir -p apk_contents/assets
    cp -r "$SRC_DIR/assets/"* apk_contents/assets/
fi

cd apk_contents && zip -q -r ../app-with-dex.apk . && cd "$BUILD_DIR"

$BUILD_TOOLS/zipalign -f 4 app-with-dex.apk app-aligned.apk

$BUILD_TOOLS/apksigner sign \
  --ks "$KEYSTORE" \
  --ks-key-alias screenshare \
  --ks-pass pass:screenshare123 \
  --key-pass pass:screenshare123 \
  --out "$OUTPUT_APK" \
  app-aligned.apk 2>&1

# Copy to versioned name
cp "$OUTPUT_APK" "$OUTPUT_DIR"

echo "=== Build Complete ==="
echo "APK: $OUTPUT_DIR"
ls -lh "$OUTPUT_DIR"
echo ""
echo "=== APK Contents ==="
unzip -l "$OUTPUT_APK" | head -30
echo ""
echo "=== File size breakdown ==="
echo "Total APK size: $(du -h "$OUTPUT_APK" | cut -f1)"
echo "Assets: $(du -sh "$SRC_DIR/assets/" 2>/dev/null | cut -f1 || echo 'N/A')"
echo "Resources: $(du -sh "$SRC_DIR/res/" 2>/dev/null | cut -f1 || echo 'N/A')"
