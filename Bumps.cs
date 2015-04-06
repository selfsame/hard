using UnityEngine;
using System.Collections;

public class Bumps : MonoBehaviour {
    void OnCollisionEnter(Collision collision) {
        SendMessage("OnBumpEnter", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnCollisionEnter2D(Collision2D collision) {
        SendMessage("OnBumpEnter2D", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnCollisionExit(Collision collision) {
        SendMessage("OnBumpExit", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnCollisionExit2D(Collision2D collision) {
        SendMessage("OnBumpExit2D", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnCollisionStay(Collision collision) {
        SendMessage("OnBumpStay", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnCollisionStay2D(Collision2D collision) {
        SendMessage("OnBumpStay2D", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnTriggerEnter(Collider collision) {
        SendMessage("OnBumpTriggerEnter", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnTriggerEnter2D(Collider2D collision) {
        SendMessage("OnBumpTriggerEnter2D", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnTriggerExit(Collider collision) {
        SendMessage("OnBumpTriggerExit", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnTriggerExit2D(Collider2D collision) {
        SendMessage("OnBumpTriggerExit2D", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnTriggerStay(Collider collision) {
        SendMessage("OnBumpTriggerStay", collision, SendMessageOptions.DontRequireReceiver);         
    }
    void OnTriggerStay2D(Collider2D collision) {
        SendMessage("OnBumpTriggerStay2D", collision, SendMessageOptions.DontRequireReceiver);         
    }
}