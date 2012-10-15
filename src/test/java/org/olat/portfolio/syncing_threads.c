#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>

int running = 1;

int thread1_waiting = 0;
pthread_t thread1;
pthread_cond_t thread1_cond;

int thread2_waiting = 0;
pthread_t thread2;
pthread_cond_t thread2_cond;

void*
thread1_func(void *arg){
  pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

  while(running){
    /* sleep 1 sec */
    usleep(1000000);

    pthread_mutex_lock(&mutex);

    if(!thread2_waiting){
      /* wait for thread 2 */
      thread1_waiting = 1;

      while(thread1_waiting){
	pthread_cond_wait(&thread1_cond,
			  &mutex);
      }

      pthread_mutex_unlock(&mutex);
    }else{
      /* wake up thread 2 */
      thread2_waiting = 0;
      pthread_mutex_unlock(&mutex);
      pthread_cond_signal(&thread2_cond);
    }
  }

  pthread_exit(NULL);
}

void*
thread2_func(void *arg){
  pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;

  while(running){
    /* sleep 1 sec */
    usleep(1000000);

    pthread_mutex_lock(&mutex);

    if(!thread1_waiting){
      /* wait for thread 2 */
      thread2_waiting = 1;

      while(thread2_waiting){
	pthread_cond_wait(&thread2_cond,
			  &mutex);
      }

      pthread_mutex_unlock(&mutex);
    }else{
      /* wake up thread 2 */
      thread1_waiting = 0;
      pthread_mutex_unlock(&mutex);
      pthread_cond_signal(&thread1_cond);
    }
  }

  pthread_exit(NULL);
}

int
main(int argc, char **argv){
  printf("creating threads: hit any key to abort\n\0");

  /* create threads */
  pthread_create(&thread1,
		 NULL, &thread1_func,
		 NULL);
  pthread_create(&thread2,
		 NULL, &thread1_func,
		 NULL);

  /* wait for input and then abort */
  getchar();
  running = 0;

  return(0);
}
