using UnityEngine;
using UnityEditor;
using System.Collections;

[System.Serializable] 
public class Extras
{

	public static bool nullObject(Object o){
		if (o == null) {
			return true;
		} else {
			return false;
		}
	}
	public static Vector3 rotateVector3(Vector3 vec, Quaternion quat){
		return quat * vec;
	}
	public static bool AddTag(string s){
		 SerializedObject tagManager = new SerializedObject(AssetDatabase.LoadAllAssetsAtPath("ProjectSettings/TagManager.asset")[0]);
		 SerializedProperty tagsProp = tagManager.FindProperty("tags");
		 
		 bool found = false;
		 for (int i = 0; i < tagsProp.arraySize; i++)
		 {
		     SerializedProperty t = tagsProp.GetArrayElementAtIndex(i);
		     if (t.stringValue.Equals(s)) { found = true; break; }
		 }
		 if (!found)
		 {
		     tagsProp.InsertArrayElementAtIndex(0);
		     SerializedProperty n = tagsProp.GetArrayElementAtIndex(0);
		     n.stringValue = s;
		     tagManager.ApplyModifiedProperties();
		     return true;
		 }
		 return false;
	}
}






 
