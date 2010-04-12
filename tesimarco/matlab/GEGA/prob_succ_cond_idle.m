function y=prob_succ_cond_idle(p,n)

i=prob_succ(p,n);
y=i/(i+prob_coll(p,n));