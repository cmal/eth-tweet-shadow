var SimpleTwitter = artifacts.require("./SimpleTwitter.sol");

contract('SimpleTwitter', async (accounts) => {
  it("should add twitter correctly", async () => {
    let instance = await SimpleTwitter.deployed();
    let empty = await instance.tweets.call();
    // assert.equal(empty, [], 'not empty. tweets: ' + empty);
    // await instance.addTweet('simple name', 'simple text');
    // let tweets = await instance.tweets.call();
    // assert.equal(tweets.length, 1, 'not has exactly ONE tweet. tweets: ' + tweets);
  });
  it("should use gas", async () => {
    const balance = web3.eth.getBalance(accounts[0]);
    let instance = await SimpleTwitter.deployed();
    await instance.addTweet('simple name', 'simple text');
    const newbalance = web3.eth.getBalance(accounts[0]);
    assert.equal(balance > newbalance, true, 'balance not changed.' + balance + ',' + newbalance);
  });

});
