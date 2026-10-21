"""Writes the small hand-made resources: themes, colours, launcher icon and the camera's file paths.

python tools/res.py
"""
from pathlib import Path

RES = Path(__file__).resolve().parent.parent / "app/src/main/res"

# A flat white from above on its saucer, with a rosetta, and a coffee ring beside it.
CUP = """    <path android:fillColor="#00000000" android:strokeColor="#99A5774B" android:strokeWidth="2.2"
        android:pathData="M72,74a11,11 0,1 1,22 0a11,11 0,1 1,-22 0z" />
    <path android:fillColor="#F4EDE0" android:pathData="M28,54a26,26 0,1 1,52 0a26,26 0,1 1,-52 0z" />
    <path android:fillColor="#EFE6D6" android:pathData="M68,50.5h12a3.5,3.5 0,0 1,0 7h-12z" />
    <path android:fillColor="#FBF7EF" android:pathData="M37,54a17,17 0,1 1,34 0a17,17 0,1 1,-34 0z" />
    <path android:fillColor="#B07C4C" android:pathData="M40,54a14,14 0,1 1,28 0a14,14 0,1 1,-28 0z" />
    <path android:fillColor="#00000000" android:strokeColor="#F7ECD8" android:strokeWidth="1.4" android:strokeLineCap="round"
        android:pathData="M47.5,57.5q6.5,3 13,0 M48.5,54.5q5.5,2.6 11,0 M49.5,51.5q4.5,2.2 9,0 M50.5,48.5q3.5,1.8 7,0 M54,46v15" />"""

FILES = {
    "values/themes.xml": """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.TheUsual" parent="Theme.AppCompat.NoActionBar">
        <item name="android:windowBackground">@color/window</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="colorPrimary">@color/amber</item>
        <item name="colorAccent">@color/amber</item>
    </style>
</resources>
""",
    "values/colors.xml": """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="window">#FF1D1712</color>
    <color name="amber">#FFF2B25C</color>
</resources>
""",
    "mipmap-anydpi-v26/ic_launcher.xml": """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_monochrome" />
</adaptive-icon>
""",
    "drawable/ic_launcher_background.xml": """<?xml version="1.0" encoding="utf-8"?>
<!-- A dark café table under a lamp: warm in the middle, dark wood at the edges. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#3A2618" android:pathData="M0,0h108v108h-108z" />
    <path android:fillColor="#4A3020" android:pathData="M0,0h108v36h-108z" />
    <path android:fillColor="#422A1B" android:pathData="M0,72h108v36h-108z" />
    <path android:fillColor="#2C1A0F" android:pathData="M0,35h108v1.5h-108z M0,71h108v1.5h-108z" />
</vector>
""",
    "drawable/ic_launcher_foreground.xml": f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
{CUP}
</vector>
""",
    "drawable/ic_monochrome.xml": """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF" android:fillType="evenOdd"
        android:pathData="M28,54a26,26 0,1 1,52 0a26,26 0,1 1,-52 0z M40,54a14,14 0,1 0,28 0a14,14 0,1 0,-28 0z" />
    <path android:fillColor="#00000000" android:strokeColor="#FFFFFF" android:strokeWidth="2.2"
        android:pathData="M72,74a11,11 0,1 1,22 0a11,11 0,1 1,-22 0z" />
</vector>
""",
    "xml/file_paths.xml": """<?xml version="1.0" encoding="utf-8"?>
<!-- The phone's camera writes the photo of a café front here; it is sketched and then thrown away. -->
<paths>
    <cache-path name="photos" path="photos/" />
</paths>
""",
}


def main():
    for name, text in FILES.items():
        f = RES / name
        f.parent.mkdir(parents=True, exist_ok=True)
        f.write_text(text, encoding="utf-8", newline="\n")


if __name__ == "__main__":
    main()
