using UnityEngine;
#if UNITY_EDITOR  
using UnityEditor;
#endif
using System.Collections;
using clojure.lang;


public class Pooled : MonoBehaviour{
	public IFn recycler;

	public void onDestroy(){
	}

	public static void addTag(string name){
		#if UNITY_EDITOR  

		 SerializedObject tagManager = new SerializedObject(AssetDatabase.LoadAllAssetsAtPath("ProjectSettings/TagManager.asset")[0]);
		 SerializedProperty tagsProp = tagManager.FindProperty("tags");
		 SerializedProperty layersProp = tagManager.FindProperty("layers");

		 bool found = false;
		 for (int i = 0; i < tagsProp.arraySize; i++)
		 {
		     SerializedProperty t = tagsProp.GetArrayElementAtIndex(i);
		     if (t.stringValue.Equals(name)) { found = true; break; }
		 }
		 
		 if (!found)
		 {
		     tagsProp.InsertArrayElementAtIndex(0);
		     SerializedProperty n = tagsProp.GetArrayElementAtIndex(0);
		     n.stringValue = name;
		 }
		 

		 SerializedProperty _spasdf = layersProp.GetArrayElementAtIndex(10);
		 if (_spasdf != null) _spasdf.stringValue = name;
		 tagManager.ApplyModifiedProperties();

		 #endif
	}

}
