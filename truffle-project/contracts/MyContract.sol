contract MyContract {
  uint public value;
  event ValueSet(uint val);
  function setValue(uint val) {
    value = val;
    ValueSet(value);
  }
  function getValue() constant returns (uint) {
    return value;
  }
}
