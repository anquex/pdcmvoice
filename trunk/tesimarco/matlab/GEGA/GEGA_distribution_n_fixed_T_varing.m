clear all;
close all;
clc;
batchsizes=[10 50 100 500 1000];
batchsizes=unique(batchsizes);
nmax=400000;
Ts=0:128
for T=Ts
    T
    if (exist(['computed.D-T-' int2str(T)],'file')~=2 && exist(['lock.D-T-' int2str(T)],'file')~=2)
        %nessuno lo sta calcolando questo T
        fid = fopen(['lock.D-T-' int2str(T)],'w+');
        fclose(fid);
        % lo prenoto
        load(['GMLE-T-' int2str(T)]); %loads GMLE(c,s,l) T b p slots
        for k=1:length(batchsizes)
            n=batchsizes(k)
            D=zeros(1,nmax);
            %se ancora non ? stato calcolato dato n e T
            if exist(['D-n-' int2str(n) '-T-' int2str(T) '.mat'],'file')~=2
                %nessuno lo ha ancora calcolato
                % ora calcolo
                for l=1:slots
                    prob_stop_slot=fG(p,n,l);
                    if (prob_stop_slot>10^-10)
                        %probabilit? dell'evento (i,s,c)
                        p_idle=prob_idle(p(l),n);
                        p_succ_cond_idle=prob_succ_cond_idle(p(l),n);
                        for c=0:T
                            for s=0:T-c
                                sc=s+c;
                                i=T-sc;
                                fiscpn=binopdf(i,T,p_idle)*binopdf(s,sc,p_succ_cond_idle);
                                D(GMLE(c+1,s+1,l))=D(GMLE(c+1,s+1,l))+fiscpn*prob_stop_slot;
                            end
                        end
                    end
                end
                save(['D-n-' int2str(n) '-T-' int2str(T)],'D','n','T');    
            end
            %fine computazione dato n e T
        end
        fid2 = fopen(['computed.D-T-' int2str(T)],'w+');
        fclose(fid2);
        delete(['lock.D-T-' int2str(T)]);
    end
end