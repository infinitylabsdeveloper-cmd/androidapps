const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Public collections: unauthenticated user can read leaderboard_entries", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("leaderboard_entries").get());
});

test("Security bound: unauthenticated user cannot write to leaderboard_entries", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("leaderboard_entries").doc("test_entry").set({
    displayName: "Hacker",
    xp: 9999
  }));
});

test("Authenticated user: can write leaderboard entry", async () => {
  const authDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(authDb.collection("leaderboard_entries").doc("alice_entry").set({
    userId: ALICE_UID,
    displayName: "Alice",
    xp: 150
  }));
});

test("Security bound: unauthenticated user cannot read xp_transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("xp_transactions").get());
});

test("Authenticated user: can read and create xp_transactions", async () => {
  const authDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(authDb.collection("xp_transactions").doc("tx_001").set({
    userId: ALICE_UID,
    amount: 50,
    reason: "quiz_completed"
  }));
  await assertSucceeds(authDb.collection("xp_transactions").doc("tx_001").get());
});
