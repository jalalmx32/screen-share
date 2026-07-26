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
VERSION=${1:-"2.2.0"}
OUTPUT_DIR="$PROJECT_DIR/android2/ScreenShare-v${VERSION}.apk"

echo "=== Building ScreenShare v${VERSION} ==="

# Clean
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"/{gen,classes,dex,compiled_res,apk_contents}

# Step 1: Compile resources
echo "[1/5] Compile resources..."
$BUILD_TOOLS/aapt2 compile --dir "$SRC_DIR/res" -o "$BUILD_DIR/compiled_res/" 2>&1

# Step 2: Link resources
echo "[2/5] Link resources..."
$BUILD_TOOLS/aapt2 link \
  --manifest "$SRC_DIR/AndroidManifest.xml" \
  -I "$ANDROID_HOME/platforms/android-34/android.jar" \
  --auto-add-overlay \
  --java "$BUILD_DIR/gen" \
  -o "$BUILD_DIR/app.unsigned.apk" \
  "$BUILD_DIR/compiled_res/"*.flat 2>&1

# Step 3: Compile Java
echo "[3/5] Compile Java..."
java -jar /tmp/ecj.jar -source 1.8 -target 1.8 \
  -classpath "$ANDROID_HOME/platforms/android-34/android.jar" \
  -d "$BUILD_DIR/classes" \
  -sourcepath "$SRC_DIR/java" \
  "$SRC_DIR/java/com/screenshare/app/MainActivity.java" 2>&1 | grep -E "error|ERROR" || echo "  OK"

# Step 4: Create DEX
echo "[4/5] Create DEX..."
cd "$BUILD_DIR/classes" && jar cf "$BUILD_DIR/all-classes.jar" . && cd "$BUILD_DIR"
d8 --output "$BUILD_DIR/dex" \
  --lib "$ANDROID_HOME/platforms/android-34/android.jar" \
  "$BUILD_DIR/all-classes.jar" 2>&1

# Step 5: Package and sign
echo "[5/5] Package & Sign..."
cd "$BUILD_DIR"
unzip -q -o app.unsigned.apk -d apk_contents/
cp dex/classes.dex apk_contents/
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

echo "=== Done ==="
ls -lh "$OUTPUT_DIR"
echo "=== Same keystore, no conflicts! ==="
