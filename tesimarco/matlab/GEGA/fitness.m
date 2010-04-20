function y=fitness(p,n,l,c,s,i)
prob_stop_slot=fG(p,n,l);
p_idle=prob_idle(p(l),n);
p_succ_cond_idle=prob_succ_cond_idle(p(l),n);
fiscpn=binopdf(i,c+s+i,p_idle)*binopdf(s,s+c,p_succ_cond_idle);
y=fiscpn*prob_stop_slot;