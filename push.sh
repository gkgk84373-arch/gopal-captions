#!/data/data/com.termux/files/usr/bin/bash
# हर बदलाव के बाद चलाओ:  ./push.sh "नया फ़ीचर"
git add -A
git commit -m "${1:-update}"
git push
echo "भेज दिया. कुछ मिनट में सबके ऐप में 'नया वर्ज़न' दिखेगा."
