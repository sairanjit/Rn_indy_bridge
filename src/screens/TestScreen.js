import React, { useState } from 'react';
import { View, Text, StyleSheet, NativeModules, TouchableOpacity } from 'react-native';
import indy from 'rn-indy-sdk';

let walletHandle

const TestScreen = ({ navigation }) => {
    const pin = 'ranjit';
    const name = '123456';

    const createWallet = async () => {
        NativeModules.Rnindy.createWallet(
            'wallet-123', 'key',
            (err) => {
                console.log(err);
                alert(err);
            },
            (message) => {
                console.log(message);
                alert(message);
            },
        );
    };

    const createAndStoreMyDid = async () => {
        NativeModules.Rnindy.createAndStoreMyDid(
            name,
            pin, "{}",
            (err) => {
                alert(err);
            },
            (message) => {
                console.log(message);
                alert(message);
            },
        );
    };

    const openWallet = async () => {
        NativeModules.Rnindy.openWallet(
            'wallet-123', 'key',
            (err) => {
                alert(err);
            },
            (message) => {
                NativeModules.Rnindy.createAndStoreMyDid();
                console.log(message);
                alert(message);
            },
        );
    };

    return (
        <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
            <TouchableOpacity onPress={() => createWallet()}>
                <View style={{ marginTop: 20, height: 70, width: 300, backgroundColor: 'blue', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: '#FFFFFF', fontSize: 20 }}>Create Wallet</Text>
                </View>
            </TouchableOpacity>

            <TouchableOpacity onPress={() => createAndStoreMyDid()}>
                <View style={{ marginTop: 20, height: 70, width: 300, backgroundColor: 'blue', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: '#FFFFFF', fontSize: 20 }}>Open wallet</Text>
                </View>
            </TouchableOpacity>``
            {/* <TouchableOpacity onPress={() => createAndStoreMyDid()}>
                <View style={{ marginTop: 20, height: 70, width: 300, backgroundColor: 'blue', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: '#FFFFFF', fontSize: 20 }}> createAndStoreMyDid</Text>
                </View>
            </TouchableOpacity> */}
        </View >
    );
};

const styles = StyleSheet.create({});

export default TestScreen;