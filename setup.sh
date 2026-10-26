#!/data/data/com.termux/files/usr/bin/bash
# सिर्फ़ एक बार चलाना है
set -e
pkg install -y openjdk-17 git gh
gh auth login

read -p "आपका GitHub username: " U
read -p "Repo का नाम [GopalCaptions]: " R
R=${R:-GopalCaptions}
read -s -p "Keystore का पासवर्ड चुनो (याद रखना): " P; echo

keytool -genkeypair -keystore release.jks -alias gopal -keyalg RSA -keysize 2048 \
  -validity 10000 -storepass "$P" -keypass "$P" -dname "CN=Gopal"

sed -i "s#GITHUB_USERNAME/GopalCaptions#$U/$R#" app/build.gradle.kts

git init -b main
git add .
git commit -m "पहला वर्ज़न"
gh repo create "$R" --public --source=. --remote=origin

base64 -w0 release.jks | gh secret set KEYSTORE_BASE64
printf '%s' "$P" | gh secret set KS_PASS
printf '%s' "gopal" | gh secret set KEY_ALIAS

git push -u origin main
echo
echo "हो गया! GitHub के Actions में APK बन रहा है (3-5 मिनट)."
echo "ज़रूरी: release.jks फ़ाइल का बैकअप कहीं सुरक्षित रखो. खो गई तो पुराने ऐप में अपडेट नहीं आएगा."
