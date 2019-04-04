var MyContract = artifacts.require("./MyContract.sol");

contract('MyContract', async (accounts) => {
  it("should set value correctly", async () => {
    let instance = await MyContract.deployed();
    let res = await instance.setValue(5);
    assert.equal(res.logs[0].args.val, 5, 'set value not equal 5.');
  });
  it("should not use gas when get value", async () => {
    let instance = await MyContract.deployed();
    let bal1 = web3.eth.getBalance(accounts[0]).toString(10);
    let value = await instance.getValue.call();
    assert.equal(value, 5, 'get value not equal 5.')
    let bal2 = web3.eth.getBalance(accounts[0]).toString(10);
    assert.equal(bal1, bal2, 'get value cost gas!');
  });
  it("value() return return value", async () => {
    let instance = await MyContract.deployed();
    let value = await instance.value.call();
    assert.equal(value, 5, 'value() not return 5');
  });

});
