package com.rnindy;

import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.crypto.CryptoResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.non_secrets.WalletRecord;
import org.hyperledger.indy.sdk.non_secrets.WalletSearch;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;
import org.hyperledger.indy.sdk.wallet.WalletStorageException;
import org.json.JSONException;
import org.json.JSONObject;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@TargetApi(24)
public class rnindyModule extends ReactContextBaseJavaModule {

    public rnindyModule(ReactApplicationContext reactContext) {
        super(reactContext); // required by React Native
        this.reactContext = reactContext;
        this.walletMap = new ConcurrentHashMap<>();
        this.poolMap = new ConcurrentHashMap<>();
        this.searchMap = new ConcurrentHashMap<>();
    }

    @Override
    // getName is required to define the name of the module represented in
    // JavaScript
    public String getName() {
        return "Rnindy";
    }

    private final ReactApplicationContext reactContext;

    private Map<Integer, Wallet> walletMap;
    private Map<Integer, Pool> poolMap;
    private Map<Integer, WalletSearch> searchMap;

    // wallet

    @ReactMethod
    public void createWallet(String id, String pin, Callback errorCallback, Callback successCallback) {
        try {
            Log.d("Hi_id", id);
            Log.d("Hi_pin", pin);
            String myWalletConfig = new JSONObject().put("id", id).toString();
            String myWalletCredentials = new JSONObject().put("key", pin).toString();
            Wallet.createWallet(myWalletConfig.toString(), myWalletCredentials).get();
            // promise.resolve(null);
            successCallback.invoke("Wallet created");
        } catch (ExecutionException e) {
            errorCallback.invoke(e.toString());
            Log.d("HI_ExecutionException", e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            errorCallback.invoke(e.toString());
            e.printStackTrace();
            Log.d("HI_InterruptedException", e.toString());
        }
        catch (IndyException e) {
            errorCallback.invoke(e.toString());
            e.printStackTrace();
            Log.d("HI_IndyException", e.toString());
        }
        catch (JSONException e) {
            errorCallback.invoke(e.toString());
            e.printStackTrace();
            Log.d("HI_JSONException", e.toString());
        }
    }

    public Wallet openWallet(String id, String pin) {
        try {
            String myWalletConfig = new JSONObject().put("id", id).toString();
            String myWalletCredentials = new JSONObject().put("key", pin).toString();
            Wallet wallet = Wallet.openWallet(myWalletConfig.toString(), myWalletCredentials).get();
            return wallet;
        } catch (ExecutionException e) {
            Log.d("HI_ExecutionException", e.toString());
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("HI_InterruptedException", e.toString());
            return null;
        }
        catch (IndyException e) {
            e.printStackTrace();
            Log.d("HI_IndyException", e.toString());
            return null;
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.d("HI_JSONException", e.toString());
            return null;
        }
    }

    @ReactMethod
    public void closeWallet(int walletHandle, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            wallet.closeWallet().get();
            walletMap.remove(walletHandle);
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void deleteWallet(String configJson, String credentialsJson, Promise promise) {
        try {
            Wallet.deleteWallet(configJson, credentialsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // did

    @ReactMethod
    public void CreateWalletAndDid(String name, String pin, Callback errorCallback, Callback successCallback) {
        try {
            PackageManager m = getCurrentActivity().getPackageManager();
            String s = getCurrentActivity().getPackageName();
            try {
                PackageInfo p = m.getPackageInfo(s, 0);
                s = p.applicationInfo.dataDir;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("yourtag", "Error Package name not found ", e);
            }
            JSONObject myWalletConfig = new JSONObject().put("id", name);
            JSONObject path = new JSONObject().put("path", s);
            myWalletConfig.put("storage_config", path);
            Log.d("HI_Config", myWalletConfig.toString());
            String myWalletCredentials = new JSONObject().put("key", pin).toString();
            Wallet.createWallet(myWalletConfig.toString(), myWalletCredentials).get();
            Wallet myWallet = Wallet.openWallet(myWalletConfig.toString(), myWalletCredentials).get();

            DidResults.CreateAndStoreMyDidResult createMyDidResult = Did.createAndStoreMyDid(myWallet, "{}").get();
            String myDid = createMyDidResult.getDid();
            String myVerKey = createMyDidResult.getVerkey();
            myWallet.closeWallet().get();
            successCallback.invoke(myDid);
            Log.d("HI_myDid", myDid);
            Log.d("HI_myVerKey", myVerKey);
        } catch (ExecutionException e) {
            Log.d("HI_ExecutionException", e.toString());
            e.printStackTrace();
            errorCallback.invoke(e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("HI_InterruptedException", e.toString());
            errorCallback.invoke(e.toString());
        } catch (IndyException e) {
            e.printStackTrace();
            Log.d("HI_IndyException", e.toString());
            errorCallback.invoke(e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            errorCallback.invoke(e.toString());
        }
    }

    @ReactMethod
    public void createAndStoreMyDid(String id, String pin, String didJson, Callback errorCallback,
                                    Callback successCallback) {
        try {
            Log.d("In try", "In try");
            Wallet w = openWallet(id, pin);
            if (w != null) {
                Log.d("In if", "In if");
                DidResults.CreateAndStoreMyDidResult createMyDidResult = Did.createAndStoreMyDid(w, didJson).get();
                String myDid = createMyDidResult.getDid();
                String myVerkey = createMyDidResult.getVerkey();
                JSONObject data = new JSONObject();
                data.put("did",myDid);
                data.put("verKey",myVerkey);
                successCallback.invoke(data);
            } else {
                errorCallback.invoke("Wallet not found");
            }
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            errorCallback.invoke(rejectResponse);
        }
    }

    @ReactMethod
    public void keyForDid(int poolHandle, int walletHandle, String did, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            Pool pool = poolMap.get(poolHandle);
            String receivedKey = Did.keyForDid(pool, wallet, did).get();
            promise.resolve(receivedKey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void keyForLocalDid(int walletHandle, String did, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String receivedKey = Did.keyForLocalDid(wallet, did).get();
            promise.resolve(receivedKey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // pairwise
    @ReactMethod
    public void createPairwise(int walletHandle, String theirDid, String myDid, String metadata, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            Pairwise.createPairwise(wallet, theirDid, myDid, metadata).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void getPairwise(int walletHandle, String theirDid, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String receivedKey = Pairwise.getPairwise(wallet, theirDid).get();
            promise.resolve(receivedKey);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // crypto

    private byte[] readableArrayToBuffer(ReadableArray arr) {
        byte[] buffer = new byte[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            buffer[i] = (byte) arr.getInt(i);
        }
        return buffer;
    }

    @ReactMethod
    public void cryptoAnonCrypt(String theirKey, ReadableArray message, Promise promise) {
        try {
            byte[] buffer = readableArrayToBuffer(message);
            byte[] encryptedData = Crypto.anonCrypt(theirKey, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : encryptedData) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void cryptoAnonDecrypt(int walletHandle, String recipientVk, ReadableArray encryptedMessage,
            Promise promise) {
        try {
            byte[] encryptedMessageBytes = readableArrayToBuffer(encryptedMessage);
            Wallet wallet = walletMap.get(walletHandle);
            byte[] decryptedData = Crypto.anonDecrypt(wallet, recipientVk, encryptedMessageBytes).get();
            promise.resolve(decryptedData);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    @Deprecated
    public void cryptoAuthCrypt(int walletHandle, String senderVk, String recipientVk, ReadableArray message,
            Promise promise) {
        try {
            byte[] buffer = readableArrayToBuffer(message);
            Wallet wallet = walletMap.get(walletHandle);
            byte[] encryptedData = Crypto.authCrypt(wallet, senderVk, recipientVk, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : encryptedData) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    @Deprecated
    public void cryptoAuthDecrypt(int walletHandle, String recipientVk, ReadableArray encryptedMessage,
            Promise promise) {
        try {
            byte[] encryptedMessageBytes = readableArrayToBuffer(encryptedMessage);
            Wallet wallet = walletMap.get(walletHandle);
            CryptoResults.AuthDecryptResult decryptedResult = Crypto
                    .authDecrypt(wallet, recipientVk, encryptedMessageBytes).get();
            String theirKey = decryptedResult.getVerkey();

            WritableArray decryptedData = new WritableNativeArray();
            for (byte b : decryptedResult.getDecryptedMessage()) {
                decryptedData.pushInt(b);
            }

            WritableArray response = new WritableNativeArray();
            response.pushString(theirKey);
            response.pushArray(decryptedData);
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void cryptoSign(int walletHandle, String signerVk, ReadableArray messageRaw, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            byte[] buffer = readableArrayToBuffer(messageRaw);
            byte[] signature = Crypto.cryptoSign(wallet, signerVk, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : signature) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void cryptoVerify(String signerVk, ReadableArray messageRaw, ReadableArray signatureRaw, Promise promise) {
        try {
            byte[] messageBuf = readableArrayToBuffer(messageRaw);
            byte[] sigBuf = readableArrayToBuffer(signatureRaw);
            boolean valid = Crypto.cryptoVerify(signerVk, messageBuf, sigBuf).get();

            promise.resolve(valid);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void packMessage(int walletHandle, ReadableArray message, ReadableArray receiverKeys, String senderVk,
            Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            byte[] buffer = readableArrayToBuffer(message);

            String[] keys = new String[receiverKeys.size()];
            for (int i = 0; i < receiverKeys.size(); i++) {
                keys[i] = receiverKeys.getString(i);
            }
            Gson gson = new Gson();
            String receiverKeysJson = gson.toJson(keys);

            byte[] jwe = Crypto.packMessage(wallet, receiverKeysJson, senderVk, buffer).get();
            WritableArray result = new WritableNativeArray();
            for (byte b : jwe) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void unpackMessage(int walletHandle, ReadableArray jwe, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            byte[] buffer = readableArrayToBuffer(jwe);
            byte[] res = Crypto.unpackMessage(wallet, buffer).get();

            WritableArray result = new WritableNativeArray();
            for (byte b : res) {
                result.pushInt(b);
            }
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // pool

    @ReactMethod
    public void setProtocolVersion(int protocolVersion, Promise promise) {
        try {
            Pool.setProtocolVersion(protocolVersion).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void createPoolLedgerConfig(String configName, String poolConfig, Promise promise) {
        try {
            Pool.createPoolLedgerConfig(configName, poolConfig).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void openPoolLedger(String configName, String poolConfig, Promise promise) {
        try {
            Pool pool = Pool.openPoolLedger(configName, poolConfig).get();
            poolMap.put(pool.getPoolHandle(), pool);
            promise.resolve(pool.getPoolHandle());
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void closePoolLedger(int handle, Promise promise) {
        try {
            Pool pool = poolMap.get(handle);
            pool.closePoolLedger().get();
            poolMap.remove(handle);
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // ledger

    @ReactMethod
    public void submitRequest(int poolHandle, String requestJson, Promise promise) {
        try {
            Pool pool = poolMap.get(poolHandle);
            String response = Ledger.submitRequest(pool, requestJson).get();
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetSchemaRequest(String submitterDid, String id, Promise promise) {
        try {
            String request = Ledger.buildGetSchemaRequest(submitterDid, id).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetSchemaResponse(String getSchemaResponse, Promise promise) {
        try {
            LedgerResults.ParseResponseResult ledgerResult = Ledger.parseGetSchemaResponse(getSchemaResponse).get();
            WritableArray result = new WritableNativeArray();
            result.pushString(ledgerResult.getId());
            result.pushString(ledgerResult.getObjectJson());
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void buildGetCredDefRequest(String submitterDid, String id, Promise promise) {
        try {
            String request = Ledger.buildGetCredDefRequest(submitterDid, id).get();
            promise.resolve(request);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void parseGetCredDefResponse(String getCredDefResponse, Promise promise) {
        try {
            LedgerResults.ParseResponseResult ledgerResult = Ledger.parseGetCredDefResponse(getCredDefResponse).get();
            WritableArray result = new WritableNativeArray();
            result.pushString(ledgerResult.getId());
            result.pushString(ledgerResult.getObjectJson());
            promise.resolve(result);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // anoncreds

    @ReactMethod
    public void proverCreateMasterSecret(int walletHandle, String masterSecretId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String outputMasterSecretId = Anoncreds.proverCreateMasterSecret(wallet, masterSecretId).get();
            promise.resolve(outputMasterSecretId);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverCreateCredentialReq(int walletHandle, String proverDid, String credentialOfferJson,
            String credentialDefJson, String masterSecretId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            AnoncredsResults.ProverCreateCredentialRequestResult credentialRequestResult = Anoncreds
                    .proverCreateCredentialReq(wallet, proverDid, credentialOfferJson, credentialDefJson,
                            masterSecretId)
                    .get();
            WritableArray response = new WritableNativeArray();
            response.pushString(credentialRequestResult.getCredentialRequestJson());
            response.pushString(credentialRequestResult.getCredentialRequestMetadataJson());
            promise.resolve(response);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverStoreCredential(int walletHandle, String credId, String credReqMetadataJson, String credJson,
            String credDefJson, String revRegDefJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String newCredId = Anoncreds
                    .proverStoreCredential(wallet, credId, credReqMetadataJson, credJson, credDefJson, revRegDefJson)
                    .get();
            promise.resolve(newCredId);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverGetCredential(int walletHandle, String credId, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String credential = Anoncreds.proverGetCredential(wallet, credId).get();
            promise.resolve(credential);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void proverGetCredentials(int walletHandle, String filter, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String credentials = Anoncreds.proverGetCredentials(wallet, filter).get();
            promise.resolve(credentials);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    // non_secrets

    @ReactMethod
    public void addWalletRecord(int walletHandle, String type, String id, String value, String tagsJson,
            Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.add(wallet, type, id, value, tagsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void updateWalletRecordValue(int walletHandle, String type, String id, String value, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.updateValue(wallet, type, id, value).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void updateWalletRecordTags(int walletHandle, String type, String id, String tagsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.updateTags(wallet, type, id, tagsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void addWalletRecordTags(int walletHandle, String type, String id, String tagsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.addTags(wallet, type, id, tagsJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void deleteWalletRecordTags(int walletHandle, String type, String id, String tagNamesJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.deleteTags(wallet, type, id, tagNamesJson).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void deleteWalletRecord(int walletHandle, String type, String id, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletRecord.delete(wallet, type, id).get();
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void getWalletRecord(int walletHandle, String type, String id, String optionsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            String record = WalletRecord.get(wallet, type, id, optionsJson).get();
            promise.resolve(record);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void openWalletSearch(int walletHandle, String type, String queryJson, String optionsJson, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletSearch search = WalletSearch.open(wallet, type, queryJson, optionsJson).get();
            searchMap.put(search.getSearchHandle(), search);
            promise.resolve(search.getSearchHandle());
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void fetchWalletSearchNextRecords(int walletHandle, int walletSearchHandle, int count, Promise promise) {
        try {
            Wallet wallet = walletMap.get(walletHandle);
            WalletSearch search = searchMap.get(walletSearchHandle);
            String recordsJson = WalletSearch.searchFetchNextRecords(wallet, search, count).get();
            promise.resolve(recordsJson);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    @ReactMethod
    public void closeWalletSearch(int walletSearchHandle, Promise promise) {
        try {
            WalletSearch search = searchMap.get(walletSearchHandle);
            WalletSearch.closeSearch(search);
            searchMap.remove(walletSearchHandle);
            promise.resolve(null);
        } catch (Exception e) {
            IndySdkRejectResponse rejectResponse = new IndySdkRejectResponse(e);
            promise.reject(rejectResponse.getCode(), rejectResponse.toJson(), e);
        }
    }

    class IndySdkRejectResponse {
        private String code;
        private String message;

        private IndySdkRejectResponse(Throwable e) {
            // Indy bridge exposed API should return consistently only numeric code
            // When we don't get IndyException and Indy SDK error code we return zero as
            // default
            String code = "0";

            if (e instanceof ExecutionException) {
                Throwable cause = e.getCause();
                if (cause instanceof IndyException) {
                    IndyException indyException = (IndyException) cause;
                    code = String.valueOf(indyException.getSdkErrorCode());
                }
            }

            String message = e.getMessage();

            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
}
