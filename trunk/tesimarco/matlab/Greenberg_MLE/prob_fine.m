function f=prob_fine(sizes,prob,slots)

% calcola la matrice f di fine ogni riga è associata a una size diversa
% mentre ogni colonna a uno slot
% prob è il vettore delle probabilità per ogni slot.

f=zeros(length(sizes),slots);
K= ones(length(sizes),slots);
for i=1:length(sizes)
    n=sizes(i);
    p=prob(1);
    f(i,1)=prob_idle(p,n)+prob_succ(p,n);
    K(i,1)=prob_coll(p,n);
    for ii=2:slots
        p=prob(ii);
        K(i,ii)=K(i,ii-1)*prob_coll(p,n);
        f(i,ii)=K(i,ii-1)*(prob_idle(p,n)+prob_succ(p,n));
    end
end