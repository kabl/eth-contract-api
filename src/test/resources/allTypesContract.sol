pragma solidity ^0.4.8;
contract allTypesContract {
    string m_string;
    bytes32 m_bytes32;
    uint m_uint;
    int m_int;
    address m_address;
    bool m_bool;

    function setString(string _data){
        m_string = _data;
    }

    function getString() constant returns (string _data) {
        _data = m_string;
    }

    function setBytes32(bytes32 _data){
        m_bytes32 = _data;
    }

    function getBytes32() constant returns (bytes32 _data) {
        _data = m_bytes32;
    }

    function setUint(uint _data){
        m_uint = _data;
    }

    function getUint() constant returns (uint _data) {
        _data = m_uint;
    }

    function setInt(int _data){
        m_int = _data;
    }

    function getInt() constant returns (int _data) {
        _data = m_int;
    }

    function setAddress(address _data){
        m_address = _data;
    }

    function getAddress() constant returns (address _data) {
        _data = m_address;
    }

    function setBool(bool _data){
        m_bool = _data;
    }

    function getBool() constant returns (bool _data) {
        _data = m_bool;
    }

    // Super unsafe but ok.
    function destroy(){
        selfdestruct(msg.sender);
    }
}