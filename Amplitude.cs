using UnityEngine;
using System;
//https://answers.unity.com/questions/1167177/how-do-i-get-the-current-volume-level-amplitude-of.html
public class Amplitude : MonoBehaviour {

 public AudioSource audioSource;
 public float updateStep = 0.1f;
 public int sampleDataLength = 512;

 private float currentUpdateTime = 0f;

 public float amplitude;
 private float[] clipSampleData;

 // Use this for initialization
 void Awake () {
 
     if (!audioSource) {
         Debug.LogError(GetType() + ".Awake: there was no audioSource set.");
     }
     clipSampleData = new float[sampleDataLength];

 }
 
 // Update is called once per frame
 void Update () {

    if (audioSource.isPlaying && audioSource.time < (audioSource.clip.length - 0.2)) {
     currentUpdateTime += Time.deltaTime;
     if (currentUpdateTime >= updateStep) {
         currentUpdateTime = 0f;
         try {
            audioSource.clip.GetData(clipSampleData, audioSource.timeSamples); 
        } catch (Exception e) {

        }
         //I read 1024 samples, which is about 80 ms on a 44khz stereo clip, beginning at the current sample position of the clip.
         amplitude = 0f;
         foreach (var sample in clipSampleData) {
             amplitude += Mathf.Abs(sample);
         }
         amplitude /= sampleDataLength;

	}
    } else {
        amplitude = 0f;
    }

}
}