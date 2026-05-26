# সাহিত্য পত্রিকা — Android App

বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়ের **সাহিত্য পত্রিকা** (ISSN: 0304-9612) –এর অফিসিয়াল Android অ্যাপ।

## বৈশিষ্ট্যসমূহ

- 📖 চলতি সংখ্যা দেখুন
- 📚 সকল সংখ্যার আর্কাইভ
- 📄 নিবন্ধ পড়ুন (সারসংক্ষেপ + PDF)
- 🔍 নিবন্ধ অনুসন্ধান করুন
- ℹ️ পত্রিকার সম্পর্কে তথ্য

## প্রযুক্তি

| বিষয় | প্রযুক্তি |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Data | Jsoup HTML parsing |
| Build | GitHub Actions |
| Min SDK | Android 8.0 (API 26) |

---

## GitHub Actions দিয়ে APK তৈরি

Android Studio ছাড়াই GitHub Actions দিয়ে APK বিল্ড করা যায়।

### ১. GitHub-এ Repository তৈরি করুন

```bash
cd ~/Desktop/ShahittoPotrikaApp
git init
git add .
git commit -m "Initial commit: Shahitto Potrika Android app"
git branch -M main
git remote add origin https://github.com/<your-username>/shahitto-potrika-app.git
git push -u origin main
```

### ২. Debug APK পাওয়া

প্রতিটি `push`-এর পরে GitHub Actions স্বয়ংক্রিয়ভাবে Debug APK তৈরি করে।
- GitHub → Repository → **Actions** ট্যাব → সর্বশেষ run → **Artifacts** → `debug-apk` ডাউনলোড করুন।

### ৩. Signed Release APK (Google Play-এর জন্য)

#### Keystore তৈরি করুন (একবারই করতে হবে)

```bash
keytool -genkey -v \
  -keystore shahitto-potrika.jks \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias shahittopotrika \
  -storepass আপনার_পাসয়ার্ড \
  -keypass আপনার_পাসয়ার্ড \
  -dname "CN=Shahitto Potrika, OU=Bangla Department, O=University of Dhaka, L=Dhaka, S=Dhaka, C=BD"
```

#### Keystore-কে Base64-এ রূপান্তর করুন

```bash
base64 -i shahitto-potrika.jks | pbcopy   # macOS (ক্লিপবোর্ডে কপি)
# অথবা
base64 -i shahitto-potrika.jks > keystore_b64.txt
```

#### GitHub Secrets যোগ করুন

GitHub Repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**:

| Secret নাম | মান |
|------------|-----|
| `KEYSTORE_BASE64` | উপরের base64 আউটপুট |
| `KEYSTORE_PASSWORD` | keystore পাসওয়ার্ড |
| `KEY_ALIAS` | `shahittopotrika` |
| `KEY_PASSWORD` | key পাসওয়ার্ড |

#### Release তৈরি করুন

```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions স্বয়ংক্রিয়ভাবে signed APK তৈরি করে GitHub Release-এ যোগ করবে।

---

## Google Play Store-এ প্রকাশ

1. [Google Play Console](https://play.google.com/console) এ লগইন করুন
2. নতুন App তৈরি করুন
3. Release APK (`app-release.apk`) আপলোড করুন
4. Store listing পূরণ করুন (বাংলায়)
5. Review-এর জন্য submit করুন

---

## License

© ১৯৫৭-২০২৬ সাহিত্য পত্রিকা, বাংলা বিভাগ, ঢাকা বিশ্ববিদ্যালয়। সর্বস্বত্ব সংরক্ষিত।
