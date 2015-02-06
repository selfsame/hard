using UnityEngine;
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
}


