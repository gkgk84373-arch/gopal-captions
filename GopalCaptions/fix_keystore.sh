#!/data/data/com.termux/files/usr/bin/bash
# keystore वाला एरर ठीक करने के लिए, एक बार चलाओ
set -e
rm -f release.jks release.b64 check.jks

read -s -p "नया Keystore पासवर्ड चुनो (याद रखना): " P; echo

keytool -genkeypair -keystore release.jks -storetype PKCS12 -alias gopal \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass "$P" -keypass "$P" -dname "CN=Gopal"

keytool -list -keystore release.jks -storepass "$P" >/dev/null && echo "keystore ठीक है"

base64 -w0 release.jks > release.b64
base64 -d release.b64 > check.jks
cmp release.jks check.jks && echo "base64 ठीक है"
rm -f check.jks

gh secret set KEYSTORE_BASE64 < release.b64
printf '%s' "$P" | gh secret set KS_PASS
printf '%s' "gopal" | gh secret set KEY_ALIAS
rm -f release.b64

git add -A
git commit -m "keystore ठीक किया, पंजाबी इंटरफ़ेस" || true
git push
echo
echo "भेज दिया. GitHub > Actions में 3-5 मिनट में नया APK बनेगा."
echo "release.jks का बैकअप सुरक्षित रखो."
