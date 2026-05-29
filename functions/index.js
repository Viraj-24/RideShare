const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
admin.initializeApp();

// Notification Function
exports.sendRideNotification = functions.database
  .ref("/notifications/{userId}")
  .onCreate(async (snapshot, context) => {
    const userId = context.params.userId;
    const notificationData = snapshot.val();

    if (!notificationData) {
      console.error("No notification data found.");
      return null;
    }

    // Retrieve FCM token for the user
    const userRef = admin.database().ref(`/Users/${userId}/fcmToken`);
    const fcmTokenSnapshot = await userRef.once("value");
    const fcmToken = fcmTokenSnapshot.val();

    if (!fcmToken) {
      console.error(`No FCM token found for user ${userId}.`);
      return null;
    }

    // Create a notification payload
    const payload = {
      notification: {
        title: notificationData.title,
        body: notificationData.body,
      },
    };

    try {
      // Send notification
      const response = await admin.messaging().sendToDevice(fcmToken, payload);
      console.log("Notification sent successfully:", response);
    } catch (error) {
      console.error("Error sending notification:", error);
    }

    return null;
  });
