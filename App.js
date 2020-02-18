import React from 'react';
import { createStackNavigator } from 'react-navigation-stack';
import { createAppContainer } from 'react-navigation';
import TestScreen from './src/screens/TestScreen';

const navigator = createStackNavigator(
  {

    Test: {
      screen: TestScreen,
      navigationOptions: {
        headerShown: false
      }
    }
  }
);

const App = createAppContainer(navigator);

export default App;
