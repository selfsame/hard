using UnityEngine;
using System.Collections;

namespace Hard{
	public static class Helper{
		public static GameObject ChildNamed(GameObject o, string n) {
			Transform[] ts = o.transform.GetComponentsInChildren<Transform>(true);
			foreach (Transform t in ts) if (t.gameObject.name == n) return t.gameObject;
			return null;
		}
	}
}