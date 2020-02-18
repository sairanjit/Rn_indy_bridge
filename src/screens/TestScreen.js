import React, { useState } from 'react';
import { View, Text, StyleSheet, NativeModules, TouchableOpacity } from 'react-native';
import indy from 'rn-indy-sdk';

let walletHandle

const TestScreen = ({ navigation }) => {
    const pin = '123456';
    const name = 'Ranji';

    // const createWallet = async () => {
    //     console.log("-----------------wallet creation started -------------");
    //     await indy.createWallet({ id: 'Ranjit' }, { key: '123456' })
    //     console.log("-----------------wallet Created -------------");
    // };

    const createWallet = async () => {
        console.log("-----------------wallet creation started -------------");
        const result = NativeModules.Rnindy.createWallet(name, pin);
        console.log("-----------------wallet Created -------------", result);
    };

    const createAndStoreMyDid = async () => {
        console.log("-----------------wallet creation started -------------");
        const result = NativeModules.Rnindy.createAndStoreMyDid(1234, 'key');
        console.log("-----------------wallet Created -------------", result);
    };

    // const createWallet = async () => {
    //     console.log('createWallet onPress')
    //     const result = await indy.createWallet({ id: 'wallet-123' }, { key: 'key' })
    //     console.log('result', result)
    // };

    const createWalletAndDid = async () => {
        NativeModules.Rnindy.CreateWalletAndDid(
            name,
            pin,
            (err) => {
                alert(err);
            },
            (message) => {
                console.log(message);
                alert(message);
            },
        );
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
            <TouchableOpacity onPress={() => deleteWallet()}>
                <View style={{ marginTop: 20, height: 70, width: 300, backgroundColor: 'blue', alignItems: 'center', justifyContent: 'center' }}>
                    <Text style={{ color: '#FFFFFF', fontSize: 20 }}> Wallet</Text>
                </View>
            </TouchableOpacity>
        </View >
    );
};

const styles = StyleSheet.create({});

export default TestScreen;

// import React from 'react'
// import { SafeAreaView, StyleSheet, ScrollView, View, Text, StatusBar, Button } from 'react-native'

// import indy from 'rn-indy-sdk'

// import { Colors } from 'react-native/Libraries/NewAppScreen'

// const TestScreen = ({ navigation }) => {
//     return (

//         <ScrollView contentInsetAdjustmentBehavior="automatic" style={styles.scrollView}>
//             <View style={styles.body}>
//                 <Text style={styles.sectionDescription}>Check console.log for test results</Text>
//                 <View style={styles.sectionContainer}>
//                     <Button title="Create wallet" onPress={createWallet} />
//                 </View>
//                 <View style={styles.sectionContainer}>
//                     <Button title="Open wallet" onPress={openWallet} />
//                 </View>
//                 <View style={styles.sectionContainer}>
//                     <Button title="Close wallet" onPress={closeWallet} />
//                 </View>
//                 <View style={styles.sectionContainer}>
//                     <Button title="Delete wallet" onPress={deleteWallet} />
//                 </View>
//             </View>
//         </ScrollView>
//     );
// }

// let walletHandle

// async function createWallet() {
//     console.log('createWallet onPress')
//     const result = await indy.createWallet({ id: 'wallet' }, { key: '123456' })
//     console.log('result', result)
// }

// async function openWallet() {
//     console.log('openWallet onPress')
//     const result = await indy.openWallet({ id: 'wallet' }, { key: '123456' })
//     walletHandle = result
//     console.log('result', result)
// }

// async function closeWallet() {
//     console.log('closeWallet onPress')
//     const result = await indy.closeWallet(walletHandle)
//     console.log('result', result)
// }

// async function deleteWallet() {
//     console.log('deleteWallet onPress')
//     const result = await indy.deleteWallet({ id: 'wallet' }, { key: '123456' })
//     console.log('result', result)
// }

// const styles = StyleSheet.create({
//     scrollView: {
//         backgroundColor: Colors.lighter,
//     },
//     body: {
//         backgroundColor: Colors.white,
//     },
//     sectionContainer: {
//         marginTop: 32,
//         paddingHorizontal: 24,
//     },
//     sectionDescription: {
//         marginTop: 8,
//         fontSize: 18,
//         fontWeight: '400',
//         color: Colors.dark,
//     },
// })

// export default TestScreen;