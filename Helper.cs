using UnityEngine;
using System.Collections;

namespace Hard{
	public static class Helper{
		public static GameObject ChildNamed(GameObject o, string n) {
			Transform[] ts = o.transform.GetComponentsInChildren<Transform>(true);
			foreach (Transform t in ts) if (t.gameObject.name == n) return t.gameObject;
			return null;
		}

		public static int Mod(int a, int b){
			return a % b;
		}



        public static Vector3 Aget(Vector3[] coll, int i)
        {
            return coll[i];
        }

        public static Quaternion Aget(Quaternion[] coll, int i)
        {
            return coll[i];
        }

        public static int Layer(int i)
        {
        	return 1 << i;
        }
    }
}