import React, { useState } from 'react';
import { View, Text, StyleSheet, NativeModules, TouchableOpacity } from 'react-native';


const TestScreen = ({ navigation }) => {
    const pin = '411042';

    const createWalletAndDid = async () => {
        NativeModules.Rnindy.CreateWalletAndDid(
            pin,
            (err) => {
                alert(err);
            },
            (message) => {
                console.log("Did : ", message);
                alert(message);
            },
        );
    };

    const openWallet = async () => {
        NativeModules.Rnindy.openWallet = ({ id: 'Protech' }, { key: pin });
        console.log("-----------wallet opened---------")
    };

    return (
        <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
            <TouchableOpacity onPress={() => createWalletAndDid()}>
                <View style={{ marginTop: 20, height: 70, width: 300, backgroundColor: 'blue', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: '#FFFFFF', fontSize: 20 }}>Create Wallet</Text>
                </View>
            </TouchableOpacity>

            <TouchableOpacity onPress={() => openWallet()}>
                <View style={{ marginTop: 20, height: 70, width: 300, backgroundColor: 'blue', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: '#FFFFFF', fontSize: 20 }}>Open Wallet</Text>
                </View>
            </TouchableOpacity>
        </View >
    );
};

const styles = StyleSheet.create({});

export default TestScreen;
