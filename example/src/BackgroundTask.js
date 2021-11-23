const wait = (time) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(true);
    }, time);
  });
};
module.exports = async (taskData) => {
  console.log('Running background task', taskData);

  await wait(5000);

  console.log('Finished background task');
};
